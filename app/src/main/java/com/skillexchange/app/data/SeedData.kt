package com.skillexchange.app.data

import com.skillexchange.app.ml.ValueEstimator

/** Demo neighbours so the app has something to show. Replace with a backend (e.g. Firebase) later. */
object SeedData {

    val providers = listOf(
        Provider("p1", "Amina Wanjiru", "Kilimani", "Lawyer who loves helping small businesses.", 4.9, 64, setOf("Programming & Tech", "Lifestyle"), "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"),
        Provider("p2", "Brian Otieno", "Westlands", "Full-stack developer, weekend podcaster.", 5.0, 132, setOf("Writing & Translation", "Business"), "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop"),
        Provider("p3", "Grace Mwende", "Kasarani", "Home cook and cooking-lesson host.", 4.8, 21, setOf("Programming & Tech", "Digital Marketing"), "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&auto=format&fit=crop"),
        Provider("p4", "Peter Kamau", "Ruaka", "Data analyst, Power BI and Excel.", 4.9, 40, setOf("Music & Audio", "Lifestyle"), "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&auto=format&fit=crop"),
        Provider("p5", "Faith Achieng", "Langata", "Writer and ebook ghostwriter.", 5.0, 9, setOf("Programming & Tech", "Video & Animation"), "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&auto=format&fit=crop"),
        Provider("p6", "Hassan Omar", "Embakasi", "Podcast editor and sound designer.", 4.7, 17, setOf("Writing & Translation", "Business"), "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&auto=format&fit=crop"),
        Provider("p7", "Mercy Njeri", "Rongai", "Explainer-video animator.", 4.9, 55, setOf("Digital Marketing", "Lifestyle"), "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300&auto=format&fit=crop"),
        Provider("p8", "Daniel Kiprop", "Kahawa", "Business-plan consultant for start-ups.", 4.6, 12, setOf("Programming & Tech", "Music & Audio"), "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=300&auto=format&fit=crop"),
    )

    private data class Seed(val owner: String, val title: String, val category: String, val subcat: String, val imageUrl: String)

    private val seeds = listOf(
        Seed("p1", "write a gdpr privacy policy and terms", "Writing & Translation", "Legal Writing", "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800&auto=format&fit=crop"),
        Seed("p2", "design and redesign a wix website", "Programming & Tech", "Website Builders & CMS", "https://images.unsplash.com/photo-1547658719-da2b51169166?w=800&auto=format&fit=crop"),
        Seed("p2", "edit and master your podcast", "Music & Audio", "Podcast Editing", "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=800&auto=format&fit=crop"),
        Seed("p3", "teach you to cook traditional swahili dishes", "Lifestyle", "Cooking Lessons", "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=800&auto=format&fit=crop"),
        Seed("p4", "build automated power bi report", "Programming & Tech", "Data Analysis & Reports", "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop"),
        Seed("p5", "write and design your ebook", "Writing & Translation", "Book & eBook Writing", "https://images.unsplash.com/photo-1471107340929-a87cd0f5b5f3?w=800&auto=format&fit=crop"),
        Seed("p5", "transcribe english audio files", "Writing & Translation", "Transcripts", "https://images.unsplash.com/photo-1589254065878-42c9da997008?w=800&auto=format&fit=crop"),
        Seed("p6", "edit and master your podcast episodes", "Music & Audio", "Podcast Editing", "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&auto=format&fit=crop"),
        Seed("p7", "create a whiteboard animated explainer video", "Video & Animation", "Whiteboard & Animated Explainers", "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=800&auto=format&fit=crop"),
        Seed("p8", "write a business plan for your start-up", "Business", "Business Plans", "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop"),
        Seed("p3", "create a personalised greeting video", "Lifestyle", "Greeting Cards & Videos", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&auto=format&fit=crop"),
    )

    /** Values every seeded service with the loaded model, using its owner's rating and review count. */
    fun services(estimator: ValueEstimator): List<Service> = seeds.mapIndexed { i, s ->
        val owner = providers.first { it.id == s.owner }
        Service(
            id = "s${i + 1}", ownerId = s.owner, title = s.title, category = s.category, subcat = s.subcat,
            valueEur = estimator.estimate(s.title, s.category, s.subcat, owner.rating, owner.reviews),
            rating = owner.rating, reviews = owner.reviews, imageUrl = s.imageUrl
        )
    }
}
