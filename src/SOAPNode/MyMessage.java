package SOAPNode;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MyMessage
{
	public static void show(String text)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText(text);
		alert.show();
	}
}