package com.hussain.learningdashboard.core.network.mock

internal object MockResponses {

    const val DEMO_EMAIL = "demo@learning.com"
    const val DEMO_PASSWORD = "password123"

    val courses = """
        [
          { "id": 1, "title": "Python Programming", "instructor": "Fahim", "progress": 65, "lessons": 20 },
          { "id": 2, "title": "Generative AI", "instructor": "Hussain", "progress": 40, "lessons": 15 },
          { "id": 3, "title": "Full Stack Development", "instructor": "Fahim", "progress": 25, "lessons": 28 }
        ]
    """.trimIndent()

    private val curriculum: Map<Int, Pair<List<String>, Int>> = mapOf(
        1 to (listOf(
            "Introduction", "Setting Up Python", "Variables & Data Types", "Operators", "Strings",
            "Lists & Tuples", "Dictionaries & Sets", "Conditionals", "Loops", "Functions",
            "Modules & Packages", "File Handling", "Error Handling", "OOP", "Inheritance",
            "Iterators & Generators", "Decorators", "Virtual Environments", "Testing with pytest", "Final Project",
        ) to 13),
        2 to (listOf(
            "What is Generative AI", "History of Neural Networks", "Transformers", "Tokenization", "Embeddings",
            "Large Language Models", "Prompt Engineering", "Retrieval-Augmented Generation", "Fine-tuning",
            "Image Generation", "Evaluation", "Safety & Alignment", "AI Agents", "Deploying Models", "Capstone",
        ) to 6),
        3 to (listOf(
            "How the Web Works", "HTML Basics", "CSS Fundamentals", "Flexbox & Grid", "JavaScript Basics",
            "DOM Manipulation", "Async JavaScript", "TypeScript", "React Fundamentals", "React State",
            "Routing", "Forms & Validation", "Node.js", "Express", "REST APIs", "Authentication",
            "SQL Databases", "ORMs", "NoSQL Databases", "Testing", "Git Workflows", "Docker",
            "CI/CD", "Cloud Deployment", "Performance", "Security", "System Design", "Capstone Project",
        ) to 7),
    )

    /** Returns null for unknown course ids so the mock server can respond 404. */
    fun lessonsFor(courseId: Int): String? {
        val (titles, completedCount) = curriculum[courseId] ?: return null
        return titles.mapIndexed { index, title ->
            """{ "id": ${courseId * 100 + index + 1}, "title": "$title", "completed": ${index < completedCount} }"""
        }.joinToString(prefix = "[", postfix = "]", separator = ",")
    }
}
