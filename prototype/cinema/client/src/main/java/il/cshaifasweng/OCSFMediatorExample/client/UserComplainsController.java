package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Complains;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class UserComplainsController {

    @FXML
    private TextArea complaint_text_area;

    @FXML
    private ComboBox<String> branch_combo_box;

    @FXML
    private Button submit_button;

    @FXML
    private TableView<Complains> complaints_table;

    @FXML
    private TableColumn<Complains, String> complaint_text_column;

    @FXML
    private TableColumn<Complains, String> status_column;

    @FXML
    private TableColumn<Complains, String> response_column;

    private String curr_id = "0";

    @FXML
    public void initialize() {
        //System.out.println("UserComplainsController.initialize");
        EventBus.getDefault().register(this); // Registering with EventBus

        // Create and send a message to request all complaints from the server
        Message request_message = new Message(74, "#GetUserComplaints");
        request_message.setObject(curr_id);
        try {
            SimpleClient.getClient().sendToServer(request_message);
            //System.out.println("Request for complaints sent to server");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the table columns
        complaint_text_column.setCellValueFactory(new PropertyValueFactory<>("complain_text"));
        status_column.setCellValueFactory(cellData -> {
                    boolean status = cellData.getValue().getStatus();
                    String statusText = status ? "Handled" : "Pending";
                    SimpleStringProperty simpleStringProperty = new SimpleStringProperty(statusText);
                    return simpleStringProperty;
                });

        response_column.setCellValueFactory(new PropertyValueFactory<>("respond"));

        // Populate the ComboBox with branches
        branch_combo_box.getItems().addAll("Haifa", "Nazareth", "Tel Aviv");
    }

    @FXML
    protected void handle_submit() {
        //System.out.println("UserComplainsController.handle_submit");
        String complaint_text = complaint_text_area.getText();
        String selected_branch = branch_combo_box.getValue();

        if (!complaint_text.isEmpty() && selected_branch != null) {
            Complains new_complaint = new Complains();
            new_complaint.setComplain_text(complaint_text);
            new_complaint.setTime_of_complain(LocalDateTime.now().toString());
            new_complaint.setRespond("");
            new_complaint.setStatus(false);
            new_complaint.setCinema_branch(selected_branch);

            submit_complaint(new_complaint);
        }
    }

    private void submit_complaint(Complains complaint) {
        //System.out.println("UserComplainsController.submit_complaint");
        Message message = new Message(75, "#SubmitComplaint");
        message.setObject(complaint);
        try {
            SimpleClient.getClient().sendToServer(message);
            //System.out.println("Complaint sent to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void handle_show_user_complaints_event(BaseEventBox event) {
        if(event.getId()==16) {
            Platform.runLater(() ->{
            List<Complains> complaintsList = (List<Complains>) event.getMessage().getObject();
            ObservableList<Complains> complaints = FXCollections.observableArrayList(complaintsList);
            complaints_table.setItems(complaints);
            System.out.println("Complaints table updated with " + complaintsList.size() + " items.");
            });
        }
    }

    @Subscribe
    public void change_content1(BeginContentChangeEnent event)
    {

        System.out.println(event.getPage());
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().post(new ContentChangeEvent(event.getPage()));
    }
}
