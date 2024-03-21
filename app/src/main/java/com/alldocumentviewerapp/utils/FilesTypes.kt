package com.alldocumentviewerapp.utils

import android.webkit.MimeTypeMap


enum class FileTypes(val mimeTypes: List<String?>) {
    PDF(
        listOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")),
    ),
    WORD(
        listOf(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx"),
        ),
    ),
    PPT(
        listOf(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx"),
        ),
    ),
    EXCEL(
        listOf(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx"),
        ),
    ),
    TEXT(
        listOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")),
    ),
    ZIP(
        listOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip")),
    ),
    RAR(
        listOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("rar")),
    ),
}