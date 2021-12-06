package com.asi;

// Java Program to create fileChooser
// and add it to the stage 

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage; 

import javafx.event.ActionEvent; 
import javafx.event.EventHandler;

import java.awt.*;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Paths;

import javafx.stage.FileChooser;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.net.InetAddress;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

public class App extends Application {

	public static String filename;
	private static final String SERVER_NAME = "pool.ntp.org";
	private volatile TimeInfo timeInfo;
	private volatile Long offset;

// launch the application 
public void start(Stage stage) 
{

	try {

		// set title for the stage
		stage.setTitle("WATERMARK -- pk@cortecsystems");

		// create a File chooser 
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		chooser.setInitialDirectory(new File(currentPath));

		// create a Label 
		Label label = new Label("no files selected");
		label.setAlignment(Pos.BOTTOM_LEFT);

        // create a text field 
        TextField txtWatermark = new TextField ("51161-06-140-142");

		// create a Button 
		Button btnChoosePdf = new Button("Select PDF");

		// create an Event Handler 
		EventHandler<ActionEvent> evtFileChoose = 
		new EventHandler<ActionEvent>() { 

			public void handle(ActionEvent e) 
			{

				if (filename != null) {
					File file = new File(filename);
					chooser.setInitialDirectory(new File(file.getParent()));
				}
				else
					chooser.setInitialDirectory(new File(currentPath));

				// get the file selected
				File file = chooser.showOpenDialog(stage);

				if (file != null) { 
					label.setText(file.getAbsolutePath() );
					filename = file.getAbsolutePath();

					System.out.println(file.getParent()+"/"+"WATERMARK-"+file.getName());
                }
                
			}
		}; 

		btnChoosePdf.setOnAction(evtFileChoose); 

        // create a Button 
		Button btnWatermark = new Button("Generate");

		// create an Event Handler 
		EventHandler<ActionEvent> evtWatermark = 
		new EventHandler<ActionEvent>() { 

			public void handle(ActionEvent e) 
			{
				try {
					// Watermark process
					File file = new File(filename);
					String sWatermarkName = txtWatermark.getText();

					manipulatePdf(file.getAbsolutePath(), file.getParent() + "/" + sWatermarkName +" " + file.getName(),sWatermarkName);
					label.setText(sWatermarkName +" " + file.getName());

				} catch(NullPointerException eNull) {
					label.setText("First select a PDF to Watermark");

				} catch (IOException ioException) {
					label.setText("First select a PDF...");
					ioException.printStackTrace();

				} catch (DocumentException documentException) {
					label.setText("First select a PDF...");
					documentException.printStackTrace();
				}
			}
		};

		btnWatermark.setOnAction(evtWatermark);

		// create a Button - OPEN FOLDER
		Button btnOpenFolder = new Button("Open Folder in Explorer");

		// create an Event Handler
		EventHandler<ActionEvent> evtOpenFolder =
				new EventHandler<ActionEvent>() {

					public void handle(ActionEvent e)
					{
						// Open PDF Folder Location
						try {
							File file = new File(filename);
							Desktop.getDesktop().open(new File(file.getParent()));

						} catch (IOException ioException) {
							label.setText("First select a PDF to Watermark");
//							ioException.printStackTrace();
						}
					 	catch(NullPointerException eNull) {
							label.setText("First select a PDF to Watermark");
						}
					}
				};

		btnOpenFolder.setOnAction(evtOpenFolder);

		btnChoosePdf.setMaxWidth(Double.MAX_VALUE);
        btnWatermark.setMaxWidth(Double.MAX_VALUE);
		btnOpenFolder.setMaxWidth(Double.MAX_VALUE);
        txtWatermark.setMaxWidth(Double.MAX_VALUE);
        label.setMaxWidth(Double.MAX_VALUE);


		// Use a border pane as the root for scene
		BorderPane border = new BorderPane();

		// create a VBox
		VBox vBoxTop = new VBox(5, btnChoosePdf, txtWatermark, btnWatermark, btnOpenFolder);
		VBox vBoxBottom = new VBox(5, label);

		border.setTop(vBoxTop);
		border.setBottom(vBoxBottom);

		// create a scene 
		Scene scene = new Scene(border, 450, 300);

		// add stylesheets
		String css = App.class.getResource("/css/wm.css").toExternalForm();
		scene.getStylesheets().clear();
		scene.getStylesheets().add(css);

		stage.getIcons().add(new Image(ClassLoader.getSystemResourceAsStream("images/stamp.png")));
//		stage.setResizable(false);

		// set the scene
		stage.setScene(scene);

		stage.show();

	}

	catch (Exception e) { 

		System.out.println(e.getMessage()); 
	} 

} 

public void manipulatePdf(String src, String dest, String sWatermark) throws IOException, DocumentException {
    PdfReader reader = new PdfReader(src);
    int n = reader.getNumberOfPages();
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
    // text watermark
    Font f = new Font(FontFamily.HELVETICA, 18);
    Phrase p = new Phrase(sWatermark, f);
    // transparency
    PdfGState gs1 = new PdfGState();
    gs1.setFillOpacity(0.9f);
    // properties
    PdfContentByte over;
    Rectangle pagesize;
    float x, y;
    // loop over every page
    for (int i = 1; i <= n; i++) {
        pagesize = reader.getPageSizeWithRotation(i);
        x = (pagesize.getRight()) - 7;
        y = (pagesize.getBottom()) + 100;
        over = stamper.getOverContent(i);
        over.saveState();
        over.setGState(gs1);
        ColumnText.showTextAligned(over, Element.ALIGN_LEFT, p, x, y, 90);
        over.restoreState();
    }
    stamper.close();
    reader.close();
}

// get ntp time & date
public void getTimeAndDate()	{

try {

		NTPUDPClient client = new NTPUDPClient();
		// We want to timeout if a response takes longer than 10 seconds
		client.setDefaultTimeout(10_000);

		InetAddress inetAddress = InetAddress.getByName(SERVER_NAME);
		TimeInfo timeInfo = client.getTime(inetAddress);
		timeInfo.computeDetails();
		if (timeInfo.getOffset() != null) {
			this.timeInfo = timeInfo;
			this.offset = timeInfo.getOffset();
		}

		// Calculate the remote server NTP time
		long currentTime = System.currentTimeMillis();
		TimeStamp atomicNtpTime = TimeStamp.getNtpTime(TimeStamp.getNtpTime(currentTime + offset).getTime());

		System.out.println("Atomic time:\t" + atomicNtpTime + "  " + atomicNtpTime.toDateString());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
	}

}

// Main Method 
public static void main(String args[]) 
	{
		// launch the application
		launch(args);
	}
} 
