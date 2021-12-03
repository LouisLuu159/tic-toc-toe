package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.Message;
import model.PlayerInfo;
import model.SessionInfo;
import model.Shapes;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import Exception.InvalidNumberOfRowsException;

import static utility.UtilityMethods.showConfirmation;
import static utility.UtilityMethods.showError;

public class Client extends Application {
	private Stage stage;
	private Scene scene;
	private GridPane playersNameSymbolAllottedGridPane;
	private Label label;

	private int NUMBER_OF_ROWS = 3;
	private static final String BACKGROUND_COLOR_BUTTON_DEFAULT_TIC = "#5465f8";
	private static final String BACKGROUND_COLOR_BUTTON_HOVER_TIC = "#6272fa";
	private static final String STYLESHEET_PATH = "./stylesheets/styles.css";

	private final SimpleStringProperty userConnectionStatus = new SimpleStringProperty(
			"Đợi những người chơi khác tham gia...");
	private final SimpleStringProperty userFullName = new SimpleStringProperty("");
	private final SimpleStringProperty topField = new SimpleStringProperty();
	private final SimpleStringProperty selectedDropdown = new SimpleStringProperty();
	private final SimpleStringProperty lobbyStatus = new SimpleStringProperty("Phòng đang trống");
	private final SimpleBooleanProperty disableAllButtons = new SimpleBooleanProperty(true);

	private ClientHandler clientHandler;
	private Socket socket;
	private SessionInfo sessionInfo = null;
	private Message selectedStartMessage = null;

	@Override
	public void start(Stage stage) {
		try {
			socket = new Socket("localhost", 6789);
		} catch (IOException e) {
			showError("Ngoại lệ xuất hiện ở Client start: " + e.toString());
		}
		clientHandler = new ClientHandler(socket);
		

		this.stage = stage;

		this.scene = getCompleteScene();
		stage.setScene(scene);
		stage.setTitle("Tic-Tac-Toe Multiplayer");
		stage.setResizable(false);
		stage.show();
	}

	private Scene getCompleteScene() {
		BorderPane borderPane = new BorderPane();

		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);
		hBox.setPadding(new Insets(50));
		hBox.setSpacing(30);
		TextField name = getTextField("Nhập nickname: ", 180, 40);
		Label label = getLabel("Nhập nickname: ");
		userFullName.bind(name.textProperty());

		hBox.getChildren().addAll(label, name);
		borderPane.setTop(hBox);

		VBox mainVBox = new VBox();
		mainVBox.setAlignment(Pos.TOP_CENTER);
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(30));
		vBox.setSpacing(50);

		Button createSession = getButtonCreateSession(borderPane);
		Button joinSession = getButtonJoinSession(borderPane);
		this.maxWidth(joinSession, 200, 200);
		this.adjustProps(joinSession);

		vBox.getChildren().addAll(createSession, joinSession);
		mainVBox.getChildren().add(vBox);
		borderPane.setLeft(mainVBox);

		Button ok = getButton("Ok", 80, 80);
		addOkayForwardFlow(ok);
		this.adjustProps(ok);

		VBox okButtonBox = new VBox();
		okButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
		okButtonBox.setPadding(new Insets(30));
		okButtonBox.getChildren().add(ok);
		borderPane.setBottom(okButtonBox);

		return new Scene(borderPane, 800, 800);
	}

	private Button getButtonCreateSession(BorderPane borderPane) {
		Button createSession = getButton("Tạo Session", 200, 200);
		this.maxWidth(createSession, 200, 200);
		this.adjustProps(createSession);
		createSession.setOnAction(e -> {
			borderPane.setCenter(null);
			this.selectedStartMessage = Message.CREATE_SESSION;
			VBox mainCreateVBox = createSessionORJoinVBox("Số lượng hàng", "Nhập số lượng hàng",
					"Số lượng người chơi cho phép", new String[] { "2", "3", "4", "5" }, true);
			borderPane.setCenter(mainCreateVBox);
		});
		return createSession;
	}

	private void addOkayForwardFlow(Button ok) {// function where logic game is handled
		ok.setOnAction(e -> {
			String userFullNameValue = userFullName.getValue().trim();
			String topFieldValue = topField.getValue();
			String selectedDropdownValue = selectedDropdown.getValue();

			if (userFullNameValue.isEmpty() || topFieldValue.isEmpty()) {
				showError("Các trường không được để trống!");
				return;
			}

			this.clientHandler.setValues(userFullNameValue, topFieldValue, selectedDropdownValue);

			this.sessionInfo = new SessionInfo();
			boolean IsJoiningGame = false;
			if (this.selectedStartMessage == Message.CREATE_SESSION) {// send player info and sessioninfo

				try {
					this.sessionInfo = this.clientHandler.initiateSession();
					this.NUMBER_OF_ROWS = Integer.parseInt(this.topField.getValue());
					changeScene();
					reloadPlayerList(this.sessionInfo.getPlayers_info());
					IsJoiningGame = true;

				} catch (InvalidNumberOfRowsException ex) {
					showError(ex.getMessage());
				}

			} else if (this.selectedStartMessage == Message.JOIN_SESSION) {// send player info and receive session info
				Object join_session_response = this.clientHandler.joinSession();
				// if join session successfully, client will receive SessionInfo object
				if (join_session_response.getClass() == SessionInfo.class) {
					sessionInfo = (SessionInfo) join_session_response;
					this.NUMBER_OF_ROWS = sessionInfo.getNumberOfRows();
					changeScene();
					reloadPlayerList(sessionInfo.getPlayers_info());
					IsJoiningGame = true;
				} else {
					Message msg = (Message) join_session_response;
					if (msg == Message.SESSION_IS_FULL)
						showError("Session đã đủ người! Vui lòng chọn session khác.");
					else if (msg == Message.NICKNAME_EXISTS)
						showError("Nickname đã tồn tại!");
					else
						showError("ID không đúng");
				}
			} else {
				showError("Hãy chọn Tạo Session hoặc Tham gia Session!");
			}
			if (IsJoiningGame) {
				new Thread(() -> {
					Platform.runLater(() -> {
						userConnectionStatus.set(
								"ID phòng: " + this.sessionInfo.getId() + "\t Hãy đợi người chơi khác tham gia...");
					});
					while (true) {
						System.out.println("list size: " + this.sessionInfo.getPlayers_info().size()
								+ ", numberPlayers: " + this.sessionInfo.getNumberOfPlayersAllowed());
						if (this.sessionInfo.getPlayers_info().size() == this.sessionInfo.getNumberOfPlayersAllowed())
							break;
						System.out.println("Not break");
						this.sessionInfo = this.clientHandler.getSessionInfo();
						reloadPlayerList(this.sessionInfo.getPlayers_info());

					}
					Platform.runLater(() -> {
						userConnectionStatus.set("Phòng đã đủ người!");
						label.getStyleClass().add("text-fill-green");
					});
					this.mainFunctionality();
				}).start();
			}
		});
	}

	private void reloadPlayerList(ArrayList<PlayerInfo> players_info) {

		if (players_info != null) {
			for (int i = 0; i < players_info.size(); i++) {
				PlayerInfo playerInfo = players_info.get(i);
				Node node = switch (playerInfo.getShape()) {
				case RECTANGLE -> getRectangle(Color.BLACK);
				case TICK -> getTick();
				case LINE -> getLine(Color.BLACK);
				case POLYGON -> getPolygon(Color.BLACK);
				default -> getCircle(Color.BLACK);
				};
				int x = i;
				Platform.runLater(() -> {
					String player_name = playerInfo.getNickname();
					if(player_name.equals(this.clientHandler.getUsername())) {
						player_name += " :You";
					}
					lobbyStatus.set("Players with Symbols");
					HBox box = boxRight(player_name, node);
					playersNameSymbolAllottedGridPane.add(box, 0, x);
				});
			}
		}

	}

	private Button getButtonJoinSession(BorderPane borderPane) {
		Button joinSession = getButton("Tham gia Session", 200, 200);
		joinSession.setOnAction(e -> {
			this.selectedStartMessage = Message.JOIN_SESSION;
			VBox mainCreateVBox = createSessionORJoinVBox("Session ID", "Nhập Session ID", "",
					new String[] { "2", "3", "4", "5" }, false);
			borderPane.setCenter(mainCreateVBox);
		});
		return joinSession;
	}

	private void mainFunctionality() {// // function where logic game is handled
		while (true) {
			boolean isIAmAllowed = this.clientHandler.receivePermission();
			System.out.println("Allowed: " + isIAmAllowed);
			if (isIAmAllowed) {
				Platform.runLater(() -> {
					userConnectionStatus.set("Đến lượt bạn!");
					label.getStyleClass().add("text-fill-green");
				});
				this.disableAllButtons.set(false);
			} else {
				Platform.runLater(() -> {
					label.getStyleClass().add("text-fill-red");
					userConnectionStatus.set("Chưa đến lượt bạn, hãy chờ nhé!");
				});
			}

			Object[] moveAndResult = this.clientHandler.readMoveAndResult();
			Node node = getNodeGraphic((Shapes) moveAndResult[0]);
			Button button = (Button) this.scene.lookup("#" + moveAndResult[1]);
			Platform.runLater(() -> button.setGraphic(node));
			String winner = (String) moveAndResult[2];
			Boolean isDraw = (Boolean) moveAndResult[3];
			if (winner != null) {
				showConfirmation(winner + " has won the game!");
				break;
			} else if (isDraw) {
				showConfirmation("The game is Draw!");
				break;
			}
		}
	}

	private VBox createSessionORJoinVBox(String textLabelTop, String placeholderText, String textLabelBottom,
			String[] comboBoxValues, boolean wants) {
		VBox mainCreateVBox = new VBox();
		mainCreateVBox.setPadding(new Insets(10));
		mainCreateVBox.setAlignment(Pos.TOP_CENTER);

		HBox numberOfRowsBox = new HBox();
		numberOfRowsBox.setAlignment(Pos.CENTER);
		numberOfRowsBox.setPadding(new Insets(10));
		numberOfRowsBox.setSpacing(30);
		Label rowsLabel = getLabel(textLabelTop);

		numberOfRowsBox.getChildren().add(rowsLabel);

		HBox playersAllowed = new HBox();
		playersAllowed.setSpacing(30);
		playersAllowed.setPadding(new Insets(10));
		playersAllowed.setAlignment(Pos.CENTER);
		Label numberOfPlayers = getLabel(textLabelBottom);
		ComboBox<String> comboBox = getStringComboBox(comboBoxValues, selectedDropdown);
		playersAllowed.getChildren().addAll(numberOfPlayers, comboBox);

		Node node;
		if (wants) {
			node = getStringComboBox(new String[] { "3", "4", "5" }, topField);
			mainCreateVBox.getChildren().addAll(numberOfRowsBox, playersAllowed);
		} else {
			TextField textField = getTextField(placeholderText, 100, 40);
			topField.bind(textField.textProperty());
			node = textField;
			mainCreateVBox.getChildren().addAll(numberOfRowsBox);
		}
		numberOfRowsBox.getChildren().add(node);
		return mainCreateVBox;
	}

	private ComboBox<String> getStringComboBox(String[] args, SimpleStringProperty simpleStringProperty) {
		ComboBox<String> comboBox = new ComboBox<>();
		for (String str : args) {
			comboBox.getItems().add(str);
		}
		comboBox.setValue(args[0]);
		simpleStringProperty.bind(comboBox.valueProperty());
		this.adjustWidthHeight(comboBox, 100, 40);
		return comboBox;
	}

	private Label getLabel(String text) {
		Label label = new Label(text);
		label.getStylesheets().add(STYLESHEET_PATH);
		label.getStyleClass().add("font-size");
		return label;
	}

	private void maxWidth(Button createSession, int height, int width) {
		createSession.setMaxHeight(height);
		createSession.setMaxWidth(width);
	}

	private Button getButton(String text, int width, int height) {
		Button button = new Button(text);
		button.getStylesheets().add(STYLESHEET_PATH);
		button.getStyleClass().addAll("button", "add-radius");
		this.adjustWidthHeight(button, width, height);
		return button;
	}

	private TextField getTextField(String placeholderText, int width, int height) {
		TextField textField = new TextField();
		textField.setPromptText(placeholderText);
		textField.setEditable(true);
		this.adjustWidthHeight(textField, width, height);
		return textField;
	}

	private void adjustWidthHeight(Control node, int width, int height) {
		node.setMinWidth(width);
		node.setMinHeight(height);
	}

	private void changeScene() {
		stage.setTitle("Tic-Tac-Toe, Client: " + userFullName.get());
		stage.setResizable(true);
		this.scene = new Scene(getGridPane());
		stage.setScene(this.scene);
		stage.show();
	}

	private BorderPane getGridPane() {
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		GridPane gridPane = new GridPane();

		HBox hBoxZero = new HBox();
		hBoxZero.setAlignment(Pos.CENTER);
		hBoxZero.setPadding(new Insets(10));
		this.label = new Label();
		this.label.setTextFill(Color.RED);
		this.label.textProperty().bindBidirectional(userConnectionStatus);
		this.label.getStylesheets().add(STYLESHEET_PATH);
		this.label.getStyleClass().add("font-size");

		hBoxZero.getChildren().add(label);
		borderPane.setTop(hBoxZero);

		for (int i = 0; i < this.NUMBER_OF_ROWS; i++) {
			HBox hBox = new HBox();
			hBox.setSpacing(10);
			hBox.setPadding(new Insets(5));
			for (int j = 0; j < this.NUMBER_OF_ROWS; j++) {
				Button button = buttonWithProps(i, j);
				hBox.getChildren().add(button);
			}
			gridPane.add(hBox, 0, i);
		}

		borderPane.setCenter(gridPane);

		GridPane rightGridPane = new GridPane();
		rightGridPane.getStylesheets().add(STYLESHEET_PATH);
		rightGridPane.getStyleClass().addAll("right-side", "add-radius");
		rightGridPane.setPadding(new Insets(20));
		rightGridPane.setVgap(10);
		playersNameSymbolAllottedGridPane = rightGridPane;

		Label noUsers = getLabel("");
		noUsers.textProperty().bindBidirectional(lobbyStatus);
		noUsers.setTextFill(Color.WHITE);

		HBox hBox = new HBox();
		hBox.getChildren().add(noUsers);
		hBox.setAlignment(Pos.CENTER);
		hBox.setMinWidth(250);

		rightGridPane.add(hBox, 0, 0);
		borderPane.setRight(rightGridPane);
		return borderPane;
	}

	private HBox boxRight(String playerName, Node node) {
		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);
		hBox.setPadding(new Insets(8));
		hBox.setSpacing(10);
		hBox.getStylesheets().add(STYLESHEET_PATH);
		hBox.getStyleClass().addAll("hBox-right", "add-radius");

		Label image = new Label();
		image.setGraphic(node);
		Label name = getLabel(playerName);
		hBox.getChildren().addAll(image, name);
		return hBox;
	}

	/**
	 * Provides the single button of BOARD, with the styles and properties
	 *
	 * @param i row-value of button position on board
	 * @param j column-value of button position on board
	 * @return Board single button
	 * @see #adjustProps
	 */
	private Button buttonWithProps(int i, int j) {
		int hW = (int) ((Toolkit.getDefaultToolkit().getScreenSize().getHeight()) - (200) - (this.NUMBER_OF_ROWS * 5))
				/ NUMBER_OF_ROWS;
		Button button = getButton("", hW, hW);
		button.setId(i + "-" + j);
		button.disableProperty().bind(disableAllButtons);
		this.adjustProps(button);
		button.setOnAction(e -> {
			if (button.getGraphic() == null) {
				disableAllButtons.set(true);
				this.clientHandler.sendMyMove(button.getId());
			}
		});
		return button;
	}

	private Node getNodeGraphic(Shapes shape) {
		switch (shape) {
		case RECTANGLE -> {
			return getRectangle(Color.WHITE);
		}
		case TICK -> {
			return getTick();
		}
		case LINE -> {
			return getLine(Color.WHITE);
		}
		case POLYGON -> {
			return getPolygon(Color.WHITE);
		}
		default -> {
			return getCircle(Color.WHITE);
		}
		}
	}

	/**
	 * @param button button to add style properties to
	 */
	private void adjustProps(Button button) {
		button.setCursor(Cursor.HAND);
		button.setStyle("-fx-background-color: " + BACKGROUND_COLOR_BUTTON_DEFAULT_TIC + ";");
		button.getStylesheets().add(STYLESHEET_PATH);
		button.getStyleClass().add("font-size");
		button.setOnMouseEntered(
				e -> button.setStyle("-fx-background-color: " + BACKGROUND_COLOR_BUTTON_HOVER_TIC + ";"));
		button.setOnMouseExited(
				e -> button.setStyle("-fx-background-color: " + BACKGROUND_COLOR_BUTTON_DEFAULT_TIC + ";"));
	}

	private Node getCircle(Color color) {
		Circle circle = new Circle();
		circle.setRadius(30);
		circle.setStroke(color);
		circle.setFill(Color.rgb(200, 200, 200, 0.0));
		circle.setStrokeWidth(4);
		return circle;
	}

	private Region getTick() {
		Region region = new Region();
		region.setMaxWidth(40);
		region.setMaxHeight(20);
		region.setPrefHeight(10);
		region.setRotate(-45);
		region.getStylesheets().add(STYLESHEET_PATH);
		region.getStyleClass().add("region");
		return region;
	}

	private Rectangle getRectangle(Color color) {
		Rectangle rectangle = new Rectangle(50, 50);
		rectangle.setStroke(color);
		rectangle.setFill(Color.rgb(200, 200, 200, 0.0));
		rectangle.setStrokeWidth(4);
		rectangle.setArcHeight(10);
		rectangle.setArcWidth(10);
		return rectangle;
	}

	private Line getLine(Color color) {
		Line line = new Line(0, 0, 50, 0);
		line.setStroke(color);
		line.setStrokeWidth(4);
		return line;
	}

	private Polygon getPolygon(Color color) {
		Polygon parallelogram = new Polygon();
		parallelogram.getPoints().addAll(30.0, 0.0, 130.0, 0.0, 100.00, 50.0, 0.0, 50.0);
		parallelogram.setFill(Color.TRANSPARENT);
		parallelogram.setStroke(color);
		parallelogram.setStrokeWidth(4);
		return parallelogram;
	}

	public static void main(String[] args) {
		launch(args);
	}

}

class ClientHandler {
	private String username;
	private String topField; // number of row in create session OR ID field in join session
	private int selectedValue;
	private SessionInfo sessionInfo;

	private ObjectInputStream fromServer;
	private ObjectOutputStream toServer;

	public ClientHandler(Socket socket) {
		if (socket != null) {
			try {
				this.toServer = new ObjectOutputStream(socket.getOutputStream());
				this.fromServer = new ObjectInputStream(socket.getInputStream());
				this.sessionInfo = new SessionInfo();
			} catch (IOException ex) {
				showError("Lỗi xảy ra ở ClientThread Constructor: " + ex.toString());
			}
		}
	}

	public SessionInfo initiateSession() throws InvalidNumberOfRowsException {
		int numberOfRows = Integer.parseInt(this.topField);
		int numberOfPlayersAllowed = selectedValue;
		if (numberOfRows <= numberOfPlayersAllowed) {
			throw new InvalidNumberOfRowsException("Vui lòng chọn số hàng lớn hơn số người chơi");
		}
		Object[] clientInfo = new Object[] { Message.CREATE_SESSION, this.username };
		this.sendObject(clientInfo);
		this.sessionInfo.setNumberOfRows(numberOfRows);
		this.sessionInfo.setNumberOfPlayersAllowed(numberOfPlayersAllowed);
		this.sendObject(sessionInfo);
		Object create_session_response = this.readObject();
		this.sessionInfo = (SessionInfo) create_session_response;
		System.out.println("ID: " + this.sessionInfo.getId());
		return this.sessionInfo;
	}

	public Object joinSession() throws ClassCastException {
		Object[] clientInfo = new Object[] { Message.JOIN_SESSION, this.username };
		this.sendObject(clientInfo);
		String sessionID = this.topField;
		this.sendObject(sessionID);
		Object join_session_response = this.readObject();
		return join_session_response;
	}

	private void sendObject(Object object) {
		try {
			this.toServer.writeObject(object);
		} catch (IOException ex) {
			showError("Error Occurred in sendObject in ClientHandler: " + ex.toString());
		}
	}

	private Object readObject() {
		try {
			return this.fromServer.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			showError("Lỗi xảy ra ở hàm readObject : " + ex.toString());
		}
		return null;
	}

	public void setValues(String username, String topField, String selectedValue) {
		this.username = username;
		this.topField = topField;
		this.selectedValue = Integer.parseInt(selectedValue);
	}

	public SessionInfo getSessionInfo() {
		SessionInfo info = (SessionInfo) this.readObject();
		this.sessionInfo = info;
		return info;
	}

	public boolean receivePermission() {
		String playerInTurnName = (String) this.readObject();
		System.out.println("Player in turn: " + playerInTurnName);
		System.out.println("My nickname: " + this.username);
		if (username.equals(playerInTurnName)) {
			return true;
		}
		return false;
	}

	public void sendMyMove(String buttonId) {
		this.sendObject(buttonId);
	}

	public Object[] readMoveAndResult() {
		return (Object[]) this.readObject();
	}
	public String getUsername() {
		return username;
	}
}
