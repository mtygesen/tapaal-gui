/*
 * DeletePetriNetObjectEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.gui.DrawingSurface;


/**
 *
 * @author Pere Bonet
 */
public class DeletePetriNetObjectEdit 
        extends UndoableEdit {
   
   PetriNetObject pnObject;
   DataLayer model;
   DrawingSurface view;
   Object[] objects;
   
   /** Creates a new instance of placeWeightEdit */
   public DeletePetriNetObjectEdit(PetriNetObject _pnObject,
            DrawingSurface _view, DataLayer _model) {
      pnObject = _pnObject;
      view = _view;
      model = _model;

      pnObject.markAsDeleted();      
   }

     
   /** */
   @Override
public void redo() {
      pnObject.delete();
   }

   
   /** */
   @Override
public void undo() {
      pnObject.undelete(model,view);
   }
   
   
   @Override
public String toString(){
      return super.toString() + " " + pnObject.getClass().getSimpleName() 
             + " [" +  pnObject.getId() + "]";
   }   
   
}
