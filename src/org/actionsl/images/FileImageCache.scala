package org.actionsl.images

import scala.collection.mutable.Map

/*
 * FileImageCache - holds all created FileIMages. Used for constructing image lists
 * based upon filenames and is used in history. Doesn't get rid of unused files from the cache
 * Not really necessary for a small application
 */
class FileImageCache(images: List[FileImage] = List[FileImage]()) {

  private val fileImageCache = Map[String, FileImage]() // The cache

  add(images)
  
  def size() = fileImageCache.size

  /*
     * addFileImages - adds a list of FileImages(s) to the cache
     */
  def add(list: List[FileImage]) = list map addEntry
  /*
   * addFileImage - adds a FileImage if its not already in the cache
   */
  private def addEntry(fileImage: FileImage) =
    if (!fileImageCache.contains(fileImage.name)) fileImageCache.put(fileImage.name, fileImage)
     
  /*
   * getFileImages - retrieves FileImages(s) from the cache by name and ignores keys that don't exist
   */
  def get(names: List[String]): List[FileImage] = {
    for {
      key <- names
      value <- fileImageCache.get(key) // if key isn't in the map its ignored
    } yield value
  }


    
}
