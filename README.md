# Skill Exchange (Android, Kotlin + Jetpack Compose)

Neighbourhood skill-exchange app with the Ridge value-estimator from `ridge_base.ipynb` running on the phone.

## 1. The model
`app/src/main/assets/ridge_model.json` is already included, exported from your fiverr.csv with
`ml/export_model.py` (alpha 5, test R2 0.339, median error EUR 13.47, same as the notebook).
To re-export after changing the notebook: `python ml/export_model.py --csv fiverr.csv --out ridge_model.json`
and copy the file over the one in assets. `RidgeModelTest` (uses `app/src/test/resources/`) checks the Kotlin
predictions equal scikit-learn's. The Profile tab shows "Ridge Regression model (...)" when it loaded.

## 2. Open and run
Android Studio (Koala or newer) -> Open this folder -> let Gradle sync -> Run on an emulator (API 24+).

## How the concept note maps to the code
| Concept note | Where |
|---|---|
| Accounts / profile | `ProfileScreen`, `LocalStore` (on-device) |
| List the skills I offer | `MySkillsScreen` (each skill is valued by the model) |
| Service valuation | `ml/ValueEstimator.kt` (`RidgeModel`) |
| Exchange fairly: cash top-up or extra service | `ml/ExchangeChecker.kt`, `ExchangeScreen` |
| Pay instead of swapping | "Pay cash" tab in `ExchangeScreen` |
| Recommend providers by skills, location, ratings | `reco/Recommender.kt`, `DiscoverScreen` |
| Ratings and service updates | `RequestsScreen` (status + star rating) |

## Prototype limits (be upfront about these in the report)
- Providers/requests are local demo data (`data/SeedData.kt`). A real backend (e.g. Firebase) is the next step.
- "Provider accepts" is a demo button; there is no second user yet.
- The model predicts Fiverr starting prices in EUR (R2 about 0.34 in the notebook). The app shows KSh using
  `Config.KES_PER_EUR` (150, an assumption). Calibrate with Kenyan prices before launch.
- The recommender is a transparent weighted score (need 40%, swap partner 25%, distance 20%, rating 15%),
  not a trained model.
