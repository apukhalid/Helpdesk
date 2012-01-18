package mvc.Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import lib.refreshTable;
import mvc.Model.Customer;
import mvc.Model.CustomerTable;
import mvc.Model.Employee;
import mvc.Model.EmployeeTable;
import mvc.View.CE_Frame;
import mvc.View.Error_Frame;

public class CEController implements Runnable{
    private Integer ID;
    private String EqualPW,choose;
    private CustomerTable c_model;
    private EmployeeTable e_model;
    private CE_Frame _view;
    
    public CEController (Integer ID,String choose, CE_Frame _view, CustomerTable c_model, EmployeeTable e_model) {
          this.ID = ID;  
          this.choose = choose;
          this.c_model = c_model;
          this.e_model = e_model;
          this._view = _view;
          addListener();
    }
    
    @Override
    public void run() {
        if (this.ID != null) {
            csearch (this.ID);
        }
        this._view.init();
    }
    
    private void addListener() {
        this._view.setbtn_cancelListener(new btn_cancelListener());
        this._view.setbtn_csearchListener(new btn_csearchListener());
        this._view.setbtn_saveListener(new btn_saveListener());
        this._view.setchb_newListener(new chb_newListener());
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
    
    class btn_csearchListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {  
            try {
                String Str = _view.edt_ID.getText();
                csearch(Integer.parseInt(Str));         
            } catch (NumberFormatException evt) {
                Error_Frame.Error("Please use only number for ID");
        }
        }
    }
        
     class btn_saveListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {  
        boolean equal = false;
        //send error message if one of these textfields are empty
        try {
            if ("".equals(_view.edt_cfirstname.getText()) || "".equals(_view.edt_clastname.getText())
            || "".equals(_view.edt_cusername.getText()) || "".equals(_view.edt_cpassword.getText())) {
                Error_Frame.Error("Please fill out: Firstname, Lastname, Username, Email and Password"); 
            } else {
                //don't hash password string if it is already hashed
                if (EqualPW.equals(_view.edt_cpassword.getText())) {
                equal = true;
            } 
                //check Checkbox then create or update customer/employee
                if (_view.chb_new.getSelectedObjects() == null) {
                    ID = Integer.parseInt (_view.edt_ID.getText());  
                    if ("Customer".equals(choose)) {
                        Customer updateCustomer = new Customer (ID,         
                        _view.edt_cfirstname.getText(),
                        _view.edt_clastname.getText(),
                        _view.edt_cusername.getText(),
                        _view.edt_cpassword.getText(),
                        _view.edt_ctelephone.getText(),
                        _view.edt_cemail.getText());
                        updateCustomer.updateCustomer (ID, equal);
                    } else {
                        String str = _view.edt_cemail.getText();
                        int trb = 0; 
                        trb = Integer.parseInt (str);  
                        Employee updateEmployee = new Employee (ID,         
                        _view.edt_cfirstname.getText(),
                        _view.edt_clastname.getText(),
                        _view.edt_cusername.getText(),
                        trb,
                        _view.edt_cpassword.getText());
                        updateEmployee.updateEmployee (ID,equal);
                    }
                } else {
                    if ("Customer".equals(choose)) {
                        ID = null;
                        Customer newCustomer = new Customer (ID,         
                        _view.edt_cfirstname.getText(),
                        _view.edt_clastname.getText(),
                        _view.edt_cusername.getText(),
                        _view.edt_cpassword.getText(),
                        _view.edt_ctelephone.getText(),
                        _view.edt_cemail.getText());
                        newCustomer.newCustomer();
                    } else {
                        ID = null;
                        String str = _view.edt_cemail.getText();
                        int trb = 0; 
                        trb = Integer.parseInt (str); 
                        Employee newEmployee = new Employee (ID,         
                        _view.edt_cfirstname.getText(),
                        _view.edt_clastname.getText(),
                        _view.edt_cusername.getText(),
                        trb,
                        _view.edt_cpassword.getText());
                        newEmployee.newEmployee();
                    }
                }
                //after update or create, refresh table 
                refreshTable A1;
                A1 = new refreshTable(c_model, e_model, null, null, null);
                A1.start();
                _view.dispose();
            }
         } catch (NumberFormatException evt) {
                Error_Frame.Error("Please use only number for ID / Troubleshooter");
         } catch (NullPointerException evt) {
                Error_Frame.Error("Wrong ID"); 
         }
        }
    }
     
     
      /*************************************
      * 
      *     Checkbox - ItemListener
      * 
      **************************************/
     
    class chb_newListener implements ItemListener{
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED){
                _view.edt_ID.setVisible(true);
            } else {
                _view.edt_ID.setVisible(false);
                _view.edt_ID.setText("");
                _view.edt_cfirstname.setText("");
                _view.edt_clastname.setText("");
                _view.edt_cusername.setText("");
                _view.edt_cemail.setText("");
                _view.edt_ctelephone.setText("");
                _view.edt_cpassword.setText("");
            }
        }
    }
    

     /**************************
     *  
     *  User defined functions
     *  
     ***************************/
    
    /*
    *  search ID and fill textfield with data
    */   
    public void csearch (Integer ID) {
        try {
            System.out.println(ID);
            System.out.println(choose);
            _view.edt_ID.setText(ID.toString());
            if ("Customer".equals(choose)) {
                String [] Array = Customer.searchCustomer(ID);
                _view.edt_cfirstname.setText(Array[0]);
                _view.edt_clastname.setText(Array[1]);
                _view.edt_cusername.setText(Array[2]);
                _view.edt_cpassword.setText(Array[3]);
                _view.edt_ctelephone.setText(Array[4]);
                _view.edt_cemail.setText(Array[5]);
                this.EqualPW = Array[3];
            } else {
                String [] Array = Employee.searchEmployee(ID);
                _view.edt_cfirstname.setText(Array[0]);
                _view.edt_clastname.setText(Array[1]);
                _view.edt_cusername.setText(Array[2]);
                _view.edt_cpassword.setText(Array[3]);
                _view.edt_cemail.setText(Array[4]);
                this.EqualPW = Array[3];
            } 
        } catch (NullPointerException E){
            Error_Frame.Error("ID not found");
        } catch (NumberFormatException E) {
            Error_Frame.Error("Please use only number for ID");
        }
    }
}
