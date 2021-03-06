package Helpdesk.java.helpdesk.mvc.Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Timestamp;
import java.util.Calendar;
import Helpdesk.java.helpdesk.mvc.Model.Comp;
import Helpdesk.java.helpdesk.lib.refreshTable;
import Helpdesk.java.helpdesk.mvc.Model.FacadeModel;
import Helpdesk.java.helpdesk.mvc.View.Ticket_Frame;
import Helpdesk.java.helpdesk.mvc.View.Error_Frame;
import Helpdesk.java.helpdesk.mvc.View.Main_Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;

public class TController implements Runnable{
    private Integer ID,noEm;
    private Main_Frame main;
    private Ticket_Frame _view;
    private String sol="",note="";
    private DefaultListModel model;
    private FacadeModel fa;
    
    public TController(Integer ID,Main_Frame main) {
        this.ID = ID;  
        this.main = main;
        this._view = new Ticket_Frame();
        this.fa = new FacadeModel();
        fa.newStatus();
        fa.newCategory();
        addListener();
    }
    
    @Override
    public void run() {
        if (this.ID != null) {
            search (this.ID);
        }
        this._view.init();
    }
    
    private void addListener() {
        this._view.setbtn_cancelListener(new btn_cancelListener());
        this._view.setbtn_saveListener(new btn_saveListener());
        this._view.setchb_newListener(new chb_newListener());
        this._view.setFocusListener(new IDFocusListener());
        init();
        ListSelection();
    }
    
      /*************************************
      * set Combobox (dropdown) models 
      * and select the value we have been searching for
      **************************************/
    
    private void init() {
        _view.cmb_status.setModel(new javax.swing.DefaultComboBoxModel(fa.Ticket_ComboBox(4).toArray(
                                  new Object[fa.Ticket_ComboBox(4).size()])));
        _view.cmb_category.setModel(new javax.swing.DefaultComboBoxModel(fa.Ticket_ComboBox(3).toArray(
                                    new Object[fa.Ticket_ComboBox(3).size()])));
        _view.cmb_eID.setModel(new javax.swing.DefaultComboBoxModel(fa.Ticket_ComboBox(2).toArray(
                               new Object[fa.Ticket_ComboBox(2).size()])));
        _view.cmb_cID.setModel(new javax.swing.DefaultComboBoxModel(fa.Ticket_ComboBox(1).toArray(
                               new Object[fa.Ticket_ComboBox(1).size()])));
        ArrayList <String> list = fa.showProductName();
        model = new DefaultListModel();
        for (int i=0; i <= list.size()-1; i++) {
            model.addElement((String)list.get(i));
        }
        _view.ls_products.setModel(model);
    }
    
    
      /*************************************
      * Multiple listselection (without pressing ctrl)
      **************************************/
    public void ListSelection () {
        _view.ls_products.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(_view.ls_products.isSelectedIndex(index0)) {
                    _view.ls_products.removeSelectionInterval(index0, index1);
                } else {
                    _view.ls_products.addSelectionInterval(index0, index1);
                }
            } 
         });
    }
    
    
      /*************************************
      * FocusListener
      * if the focus of ID textfield lose, 
      * it will automatically search the ID 
      **************************************/
    private class IDFocusListener extends FocusAdapter {
    @Override
        public void focusLost(FocusEvent arg0) {
                try {
                    String Str = _view.edt_ID.getText();
                    if (!Str.isEmpty()) {
                        search(Integer.parseInt (Str));  
                }
            } catch (NullPointerException E){
                Error_Frame.Error("ID not found\n\n" + E.getMessage());
            } catch (NumberFormatException E) {
                Error_Frame.Error("Please use only number for ID\n\n" + E.getMessage());
            }
        }
    }
    
    
      /*************************************
      * 
      *     ButtonListener
      * 
      **************************************/

    class btn_cancelListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {  
            _view.dispose();
        }
    }
    
      /*************************************
      *   Ticket edit save button
      *   TODO - reduce the count of if clause
      **************************************/
    class btn_saveListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {  
            FacadeModel fa = new FacadeModel();
            fa.newCategory();
            fa.newStatus();
            //There are no identical primary key 
            //so it have to delete all involved product from the ticket
            if (ID != null) {
                fa.deleteInvProduct(ID);
            }
            
           try {
            //Set timestamp for "create tickets" and "update tickets"
            Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
            //Send Error frame if one of these textfield are empty
            if (_view.edt_topic.getText().isEmpty() || (_view.edt_problem.getText().isEmpty())){
                Error_Frame.Error("Please fill out: Topic and Problem"); 
            } else {
                    //It is important to check if solution or note are empty or same as before
                    //else the history in the db will update an empty string
                 
                    if (!_view.edt_solution.getText().isEmpty() && !sol.equals(_view.edt_solution.getText())) {
                        sol = _view.edt_solution.getText();
                    }
                 
                    if (!_view.edt_note.getText().isEmpty() && !note.equals(_view.edt_note.getText())) {
                        note = _view.edt_note.getText();
                    }
                    
                    if (_view.cmb_eID.getSelectedItem() != "") {
                        noEm = Comp.getEID((String)_view.cmb_eID.getSelectedItem());
                    }

                //Check checkbox "new Ticket"
                if (_view.chb_new.getSelectedObjects() == null) {
                    ID = Integer.parseInt (_view.edt_ID.getText());  
                    //Update Ticket block ###################################################
                    fa.updateTicket(ID,
                    (Integer)_view.cmb_cID.getSelectedItem(),noEm,
                    fa.getCategoryModel().getCategoryObjectID((String)_view.cmb_category.getSelectedItem()),
                    fa.getStatusModel().getStatusObjectID((String)_view.cmb_status.getSelectedItem()),
                    _view.edt_topic.getText(), _view.edt_problem.getText(),
                    note, sol, _view.edt_created.getText(), currentTimestamp.toString());
                    //######################################################################
                    
                    //Set products  #########################################################
                    if (_view.ls_products.getSelectedIndices().length != 0) {
                        //Integer value a begins at -1 so if it reach the while clause 
                        // a will set to 0
                        Integer a = -1;

                        Object Array [] = _view.ls_products.getSelectedValues();
                        Object _intarr[] = new Object [Array.length];
                        //Looking for the same product name and save it into a array
                        for (int i=0; i<= Array.length-1; i++) {
                             do {
                                 a++;
                             } while (!fa.getProductTable().getValueAt(a, 1).equals(Array[i]));
                             _intarr [i] = fa.getProductTable().getValueAt(a, 0);
                         }
                        //Create new involved product/s
                        fa.newInvProduct(ID, _intarr);
                    }
                    //######################################################################
                    
                } else {

                    //Create Ticket block ##################################################
                    Integer _int = fa.newTicket(null,(Integer)_view.cmb_cID.getSelectedItem(),noEm,
                    fa.getCategoryModel().getCategoryObjectID((String)_view.cmb_category.getSelectedItem()),
                    fa.getStatusModel().getStatusObjectID((String)_view.cmb_status.getSelectedItem()),
                    _view.edt_topic.getText(),_view.edt_problem.getText(),note,sol,currentTimestamp.toString(),
                    currentTimestamp.toString());
                    //#######################################################################
                    
                    //Set products  #########################################################
                    //Nearly same function as above, only it takes insert_id from the database 
                    //because new involved products do not have a id
                    if (_view.ls_products.getSelectedIndices().length != 0) {
                        Integer a = -1;
                        Object Array [] = _view.ls_products.getSelectedValues();
                        Object _intarr[] = new Object [Array.length];
                        for (int i=0; i<= Array.length-1; i++) {
                             do {
                                 a++;
                             } while (!fa.getProductTable().getValueAt(a, 1).equals(Array[i]));
                             _intarr [i] = fa.getProductTable().getValueAt(a, 0);
                         }
                        fa.newInvProduct(_int, _intarr);
                    }
                    //########################################################################
                    
                }
                //Refresh Fullticket and History table
                new refreshTable("", "", "Fullticket", "History", "", main).start();
                _view.dispose();
            }
        } catch (NumberFormatException ev) {
              Error_Frame.Error("Please use only number for ID\n\n" + ev.getMessage());
        } catch (Exception ev) {
              Error_Frame.Error(ev.toString()); 
        }
        }
    }
 
    
      /*************************************
      * Checkbox - ItemListener
      * only the db can create id number so we do not need 
      * ID textfield (db ID -> auto increment)
      **************************************/
     
    class chb_newListener implements ItemListener{
        @Override
        public void itemStateChanged(ItemEvent e) {
         if (e.getStateChange() == ItemEvent.DESELECTED){
            _view.edt_ID.setVisible(true);
         } else {
            _view.edt_ID.setVisible(false);
            _view.edt_ID.setText("");
            _view.edt_problem.setText("");
            _view.edt_topic.setText("");
            _view.edt_note.setText("");
            _view.edt_solution.setText("");
            _view.edt_created.setText("");
            _view.edt_update.setText("");
        }
        }
    }

     /*************************************
      * Search the ID from the db 
      * and fill the textfield with data
      **************************************/
    public void search (Integer ID) {
        try {
            String [] Array = fa.searchTicket(ID);
            _view.edt_ID.setText(ID.toString());
            _view.edt_topic.setText(Array[4]);
            _view.edt_problem.setText(Array[5]);
            _view.edt_note.setText(Array[6]);
            _view.edt_solution.setText(Array[7]);
            _view.edt_created.setText(Array[8]);
            _view.edt_update.setText(Array[9]);
            
            if (Array[7] != null) {
                this.sol = Array[7];
            }
            if (Array[6] != null) {
                this.note = Array[6];
            }
            //Loop - looking for same Customer ID and select it
            for (int i=0; i< _view.cmb_cID.getItemCount();i++) {
                if (Array[0].equals(_view.cmb_cID.getItemAt(i).toString())) {
                    _view.cmb_cID.setSelectedIndex(i);
                 }
            }
            
            //Get the employee name from the db
            //Loop - looking for same Employee name and select it
            if (Array[1]!=null) {
               String username = Comp.getEUsername(Integer.parseInt(Array[1]));
                for (int i=0; i< _view.cmb_eID.getItemCount();i++) {
                    if (username.equals(_view.cmb_eID.getItemAt(i).toString())) {
                        _view.cmb_eID.setSelectedIndex(i);
                    }
                }
            }
            
            //Get all products from the db 
            //and compare list with product 
            Object [] product = Comp.searchTicketProduct(ID).toArray();
            int [] indices = new int [product.length];
            int a=-1;
            for (int i=0; i<product.length; i++) {
               do {
                   a++;
                } while (!_view.ls_products.getModel().getElementAt(a).equals(product[i]));
                   //save index into a array
                   indices [i] = a;  
            }
            _view.ls_products.setSelectedIndices(indices);
            
           _view.cmb_category.setSelectedIndex(Integer.parseInt(Array[2])-1);
           _view.cmb_status.setSelectedIndex(Integer.parseInt(Array[3])-1);
           //Need ID != null if we press Ticketbutton and search ticket
           //otherwise the function "delete InvProduct" will not start and we get an error msg (duplicate primary key)
           this.ID = ID;
        } catch (NullPointerException E){
            Error_Frame.Error("ID not found\n\n" + E.getMessage());
        } catch (NumberFormatException E) {
            Error_Frame.Error(E.getMessage());
        } catch (Exception E) {
            Error_Frame.Error(E.getMessage());
        }
    }
}