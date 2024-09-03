package com.alldocumentviewerapp.models

data class AllFolders(
    var folderName:String="",
    var totalFiles:String="",
    var fileModel:ArrayList<TotalFilesModel> = ArrayList()
)