package org.actionsl.utils

import scala.collection.mutable.Stack

trait HistoryTrait[T]  { // The exposed interface
  def push(elem: T)
  def undo: Option[T] 
  def redo: Option[T]
  def clear()
}

/**
 * History = rough implementation that seems to work ok
 */
class History[T] extends HistoryTrait[T] {

  private val undoStack = new Stack[T]()
  private val redoStack = new Stack[T]()
  

  private var notUndoing = true // Used to remove an extra history element at start of undo
  private var notRedoing = true // Used to remove an extra history element at start of redo

  def push(elem: T) = {
    undoStack.push(elem)
   }
  
  // used for testing
  def undoTop: T = undoStack.top 
  def redoTop: T = redoStack.top 
  
  def undo: Option[T] = {
    
   if (notUndoing) {
      notUndoing = false
      notRedoing = true
      undo // get rid of (unwanted) top element
    }
   
    if (undoStack.isEmpty) {
      None
    } else {
      var elem = undoStack.pop
      redoStack.push(elem)
      Some(elem)
    }
  }

  def redo: Option[T] = {
    
    if (notRedoing) {
      notRedoing = false
      notUndoing = true
      redo // get rid of (unwanted) top element
    }
    
    if (redoStack.isEmpty) {
      None
    } else {
      val elem = redoStack.pop
      undoStack.push(elem)
      Some(elem)
    }
  }

  def clear() {
    if(!undoStack.isEmpty) {
    val current = undoStack.top // keep last entry on the undo stack
    undoStack.clear
    push(current)
      
    }
    
    redoStack.clear
    notUndoing = true
    notRedoing = true
  }
}

object History {
  def apply[T]() = new History[T]()
}