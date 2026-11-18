package com.sweak.qralarm.core.domain.recognition

/** Canonical names from the bundled EfficientDet-Lite0 COCO label map. Never persist UI text. */
object ObjectCategories {
    val ids: List<String> = listOf(
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
        "chair", "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop",
        "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink",
        "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    ).sorted()

    fun displayName(id: String): String = id.replaceFirstChar { it.uppercaseChar() }

    fun isValid(method: String, categoryId: String?, customObjectCount: Int = 0): Boolean =
        method in setOf("NONE", "CODE", "OBJECT", "CUSTOM_OBJECT") &&
            (method != "OBJECT" || categoryId in ids) &&
            (method != "CUSTOM_OBJECT" || customObjectCount > 0)
}
