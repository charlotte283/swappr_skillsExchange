"""
Export the Ridge value-estimator from ridge_base.ipynb to a JSON file that the
Android app reads (app/src/main/assets/ridge_model.json).

Usage (same folder as fiverr.csv, e.g. in Colab or locally):
    python export_model.py --csv fiverr.csv --out ridge_model.json

It repeats the notebook EXACTLY (same cleaning, same features, same 80/20 split
with random_state=42, same 5-fold GridSearchCV over the same alpha grid), then
writes every number the app needs to reproduce model.predict() on the phone:
  * median imputer + StandardScaler values for rating / log_reviews / has_reviews
  * one-hot categories for Category and Subcat
  * TF-IDF vocabulary, idf weights and English stop-words
  * the Ridge intercept and coefficients
Prediction in the app = expm1(intercept + sum(coef * feature)).
"""
import argparse
import json

import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.feature_extraction.text import ENGLISH_STOP_WORDS, TfidfVectorizer
from sklearn.impute import SimpleImputer
from sklearn.linear_model import Ridge
from sklearn.metrics import median_absolute_error, r2_score, mean_squared_error
from sklearn.model_selection import GridSearchCV, KFold, train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

RANDOM_STATE = 42
NUM_COLS = ["rating", "log_reviews", "has_reviews"]
CAT_COLS = ["Category", "Subcat"]
TEXT_COL = "service"


def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    """Notebook section 2, unchanged."""
    clean = df.copy()
    clean = clean.drop(columns=["Category-href", "Subcat-href"])
    clean["price_eur"] = (
        clean["price"].str.extract(r"€\s*([\d,]+\.?\d*)")[0]
        .str.replace(",", "", regex=False).astype(float)
    )
    parts = clean["stars"].str.extract(r"^([\d.]+)\(([\d.]+)(k?)\+?\)$")
    clean["rating"] = parts[0].astype(float)
    clean["reviews"] = parts[1].astype(float) * np.where(parts[2] == "k", 1000, 1)
    clean["reviews"] = clean["reviews"].fillna(0)
    clean["has_reviews"] = clean["stars"].notna().astype(int)
    clean["Subcat"] = clean["Subcat"].str.replace(r"(?<=[a-z])(New|Beta)$", "", regex=True).str.strip()
    clean["service"] = clean["name"].str.lower().str.replace(r"^i will\s+", "", regex=True).str.strip()
    clean = clean.drop(columns=["name", "stars", "price"])
    clean = clean.drop_duplicates().reset_index(drop=True)
    clean["log_price"] = np.log1p(clean["price_eur"])
    clean["log_reviews"] = np.log1p(clean["reviews"])
    return clean


def make_preprocessor():
    """Notebook section 4, unchanged."""
    return ColumnTransformer([
        ("num", Pipeline([("fill", SimpleImputer(strategy="median")),
                          ("scale", StandardScaler())]), NUM_COLS),
        ("cat", OneHotEncoder(handle_unknown="ignore"), CAT_COLS),
        ("txt", TfidfVectorizer(ngram_range=(1, 2), min_df=2, max_features=3000,
                                stop_words="english"), TEXT_COL),
    ])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--csv", default="fiverr.csv")
    ap.add_argument("--out", default="ridge_model.json")
    ap.add_argument("--reference", type=int, default=400,
                    help="how many real listings to ship for 'extra service of similar value' suggestions")
    args = ap.parse_args()

    clean = clean_data(pd.read_csv(args.csv))
    X = clean[NUM_COLS + CAT_COLS + [TEXT_COL]]
    y = clean["log_price"]
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=RANDOM_STATE)

    pipe = Pipeline([("prep", make_preprocessor()), ("model", Ridge())])
    cv = KFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    grid = GridSearchCV(pipe, {"model__alpha": [0.01, 0.1, 0.5, 1, 2, 5, 10, 20, 50, 100]},
                        cv=cv, scoring="neg_root_mean_squared_error")
    grid.fit(X_train, y_train)
    best = grid.best_estimator_

    pred = best.predict(X_test)
    metrics = {
        "alpha": float(grid.best_params_["model__alpha"]),
        "test_rmse_log": float(np.sqrt(mean_squared_error(y_test, pred))),
        "test_r2": float(r2_score(y_test, pred)),
        "test_median_error_eur": float(median_absolute_error(np.expm1(y_test), np.expm1(pred))),
    }
    print("Exported model metrics:", metrics)

    prep = best.named_steps["prep"]
    ridge = best.named_steps["model"]
    coef = ridge.coef_
    num_t = prep.named_transformers_["num"]
    imputer, scaler = num_t.named_steps["fill"], num_t.named_steps["scale"]
    ohe = prep.named_transformers_["cat"]
    tfidf = prep.named_transformers_["txt"]

    pos = 0
    numeric = {}
    for i, name in enumerate(NUM_COLS):
        numeric[name] = {"impute": float(imputer.statistics_[i]), "mean": float(scaler.mean_[i]),
                         "scale": float(scaler.scale_[i]), "coef": float(coef[pos])}
        pos += 1
    categorical = {}
    for name, cats in zip(CAT_COLS, ohe.categories_):
        categorical[name] = {"values": [str(c) for c in cats],
                             "coef": [float(c) for c in coef[pos:pos + len(cats)]]}
        pos += len(cats)
    vocab = tfidf.vocabulary_
    terms = [None] * len(vocab)
    for term, idx in vocab.items():
        terms[idx] = {"t": term, "idf": float(tfidf.idf_[idx]), "w": float(coef[pos + idx])}
    pos += len(vocab)
    assert pos == len(coef), "coefficient slicing does not add up"

    taxonomy = clean.groupby("Category")["Subcat"].apply(lambda s: sorted(s.unique())).to_dict()
    cat_median = clean.groupby("Category")["price_eur"].median().round(2).to_dict()
    ref = clean.sample(min(args.reference, len(clean)), random_state=RANDOM_STATE)
    reference = [{"service": r.service, "subcat": r.Subcat, "category": r.Category,
                  "price": float(r.price_eur)} for r in ref.itertuples()]

    out = {
        "version": 1,
        "target": "log1p(price_eur)",
        "currency": "EUR",
        "metrics": metrics,
        "intercept": float(ridge.intercept_),
        "numeric": numeric,
        "categorical": categorical,
        "tfidf": {"ngram_min": 1, "ngram_max": 2, "stop_words": sorted(ENGLISH_STOP_WORDS), "terms": terms},
        "taxonomy": taxonomy,
        "category_median_eur": cat_median,
        "reference_listings": reference,
    }
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, separators=(",", ":"))
    print("Wrote", args.out)

    # Self-test examples the Android unit test can use (prediction from sklearn for 5 test rows)
    sample = X_test.head(5)
    checks = []
    for (_, r), p in zip(sample.iterrows(), best.predict(sample)):
        checks.append({"service": r[TEXT_COL], "category": r["Category"], "subcat": r["Subcat"],
                       "rating": None if pd.isna(r["rating"]) else float(r["rating"]),
                       "reviews": float(np.expm1(r["log_reviews"])), "expected_eur": float(np.expm1(p))})
    with open(args.out.replace(".json", "_selftest.json"), "w") as f:
        json.dump(checks, f, indent=1)
    print("Wrote self-test examples for the Kotlin unit test")


if __name__ == "__main__":
    main()
