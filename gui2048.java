// This program produces a GUI-based recreation of 2048!
//
//Author: Balihaar Gill
//EECS 1510
//Date: May 3, 2018
//Dr. Thomas
import javafx.scene.text.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import javafx.application.*;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.animation.*;
import javafx.beans.property.*;

public class gui2048 extends Application
{

	static boolean win = false; // as long as this is false, check for a winner. only true after a game is won.
	static boolean lose = false;
	static int moves = 0;
	static int howManyUndos = 0;
	static int[][] boardUndo = new int[4][4];
	static int counter = 0;
	static boolean moveMade = false; // used to verify that a valid move is made
	static int score = 0;

	public static void main(String[] args)
	{
		Application.launch(args);
	}

	@Override // override start method from Application
	public void start(Stage primaryStage)
	{
		// a new board is built
		// two random numbers are placed
		// the board is displayed and an input is asked for
		int[][] board = new int[4][4]; // creation of a 4x4 0-15 2D array
		for (int i = 0; i < 4; i++) // 4 rows...
		{
			for (int j = 0; j < 4; j++) // 4 columns...
			{
				board[i][j] = 0; // input x at this index value
			}

		}
		//two random numbers are generated to start the game
		randomNumberGen(board);
		randomNumberGen(board);

		Rectangle background = new Rectangle(618, 618); // rectangle node with proper size
		background.setX(0);
		background.setY(0);
		background.setFill(Color.rgb(179, 164, 152)); // change color to proper background color
		background.setArcWidth(20);
		background.setArcHeight(20);

		Pane gameBoard = generateGameBoard(board);
		Pane guiGameBoard = new Pane(); // creates the background of the game board
		guiGameBoard.getChildren().add(background);
		guiGameBoard.getChildren().add(gameBoard);

		Pane programBackground = new Pane(); // creates the background of the program
		Rectangle backgroundTheme = new Rectangle(618, 900); // rectangle node with proper size
		backgroundTheme.setFill(Color.rgb(235, 224, 214)); // change color to proper background color

		//SCORE CREATION
		IntegerProperty scoreProperty = new SimpleIntegerProperty(score); // to make the score bindable
		Text scoreText = new Text();
		scoreText.textProperty().bind(scoreProperty.asString()); // bind score to the text

		scoreText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
		scoreText.setFill(Color.rgb(255, 255, 255));
		scoreText.setX(275);
		scoreText.setY(700);

		Text scoreLabelText = new Text("Score:"); // creates a label for the changing score
		scoreLabelText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
		scoreLabelText.setFill(Color.rgb(255, 255, 255));
		scoreLabelText.setX(30);
		scoreLabelText.setY(700);
		
		//MOVE CREATION
		IntegerProperty moveProperty = new SimpleIntegerProperty(moves); // to make the score bindable
		Text moveText = new Text();
		moveText.textProperty().bind(moveProperty.asString()); // bind score to the text

		moveText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
		moveText.setFill(Color.rgb(255, 255, 255));
		moveText.setX(290);
		moveText.setY(850);

		Text moveLabelText = new Text("Moves:"); // creates a label for the changing move counter
		moveLabelText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
		moveLabelText.setFill(Color.rgb(255, 255, 255));
		moveLabelText.setX(30);
		moveLabelText.setY(850);

		// creating save and load buttons
		Button btSave = new Button("_Save");
		btSave.setLayoutX(500);
		btSave.setLayoutY(700);
		Button btLoad = new Button("_Load");
		btLoad.setLayoutX(500);
		btLoad.setLayoutY(750);

		btSave.setOnAction(e -> // if save
		{
			//pressing the save button runs the save board method
			saveBoard(board);
		});

		btSave.setMnemonicParsing(true); //creates ALT SHORTCUTS based on first letter of button label
		btLoad.setMnemonicParsing(true); //creates ALT SHORTCUTS based on first letter of button label

		btLoad.setOnAction(e -> // if load
		{ 
			//pressing the load button runs the load method and then clears and replaces the game board with the newly loaded game board
			loadBoard(board);
			gameBoard.getChildren().clear(); // clear the gui game board
			guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
		});

		//programBackground gathers all panes to create the overall GUI of the program
		programBackground.getChildren().add(backgroundTheme);
		programBackground.getChildren().add(guiGameBoard);
		programBackground.getChildren().addAll(scoreText, scoreLabelText);
		programBackground.getChildren().addAll(moveText, moveLabelText);
		programBackground.getChildren().addAll(btSave, btLoad);

		
		Scene scene = new Scene(programBackground, 618, 900);
		primaryStage.setResizable(false); //no resizing allowed!
		primaryStage.setTitle("2048 by Balihaar Gill");
		primaryStage.setScene(scene);
		primaryStage.show(); //SHOW THE SCENE!!!

		
		//requests focus on the gameBoard and denys focus to the save and load buttons so they aren't pressed or selected with arrow keys
		guiGameBoard.requestFocus();
		guiGameBoard.focusedProperty();
		btSave.setFocusTraversable(false);
		btLoad.setFocusTraversable(false);
		
		//this section controls key inputs through switch statements
		scene.setOnKeyPressed(e ->
		{
			// based on the key pressed an event is triggered to move the tiles in the
			// specified direction.
			switch (e.getCode())
			{

			case H: // HELP
			{
				if (e.isAltDown()) //make sure alt is pressed in order to allow these actions
				{
					// a help window is created to display instructions with an okay button to close it and continue playing
					Stage help = new Stage(); // new window
					help.setTitle("Help");

					VBox textAndButtons = new VBox();
					Label helpText = new Label(
							"HOW TO PLAY: Use your arrow keys to move the tiles. When two tiles with the same number touch, they merge into one!"
									+ "You can undo with ALT+Z up to ten times. Save with ALT+S load with ALT+L or use the buttons.");
					textAndButtons.setAlignment(Pos.CENTER);
					helpText.setWrapText(true);
					Button btOk = new Button("Ok");

					textAndButtons.getChildren().addAll(helpText, btOk); // Top: text Bottom: buttons

					Scene helpScene = new Scene(textAndButtons, 300, 300);
					help.setScene(helpScene);
					help.show();

					btOk.setOnAction(event -> // if OK
					{

						help.close(); // close the help window
					});
					break;
				}
				break;
			}
			
			case X: // EXIT
			{
				if (e.isAltDown()) //make sure alt is pressed
				{
					System.exit(0); //close program
					break;
				}
				break;
			}
			
			case Z: //undo
			{
				if (e.isControlDown()) //make sure control is pressed
				{
					undoLoadBoard(board); //undo method is ran
					gameBoard.getChildren().clear(); // clear the gui game board
					guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
					break;
				}
				break;
			}
			
			//Up,down,left,right all work in similar ways. The score is updated, the respective movement method is ran,
			//winChecker is ran if the game hasn't been won yet, if a legit move is made a random number is generated.
			//the board is checked for loss, if loss is true a message is displayed showing exactly that. scores from the current move are added to the total
			
			//the lose pane has a high score checker within it if it need to display and record a new highscore.
			//the lose screen is displayed for two seconds before exiting the program
			case UP:
			{
				scoreProperty.setValue(score);
				upAction(board);
				if (win == false)
				{
					winChecker(board);
				}
				if (moveMade == true) // if a valid move is made generate a random number
				{
					randomNumberGen(board);
				}
				loseChecker(board);
				gameBoard.getChildren().clear(); // clear the gui game board
				guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
				if (lose == true)
				{
					// if the game is lost, create a message displaying that the player lost.

					Rectangle loseRect = new Rectangle(500, 200);
					loseRect.setFill(Color.rgb(255, 164, 152)); // change color to proper background color
					loseRect.setArcWidth(20);
					loseRect.setArcHeight(20);

					Text loseText = new Text("You lost :(");
					loseText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					loseText.setFill(Color.rgb(249, 245, 239));
					
					StackPane losePane = new StackPane();
					losePane.setLayoutY(200);
					losePane.setLayoutX(59);
					losePane.getChildren().addAll(loseRect, loseText);

					programBackground.getChildren().add(losePane);
					
					highScoreChecker();
					PauseTransition lossTimer = new PauseTransition(Duration.seconds(2)); //after two seconds...
					lossTimer.setOnFinished(event -> primaryStage.close()); //close the program
					lossTimer.play();
				}
				score = (score + counter); // add scores made in move to running total
				if (moveMade == true)
				{
					moves = moves + 1;
				}
				scoreProperty.setValue(score);
				moveProperty.setValue(moves);
				counter = 0;
				break;
			}
			case DOWN:
			{
				downAction(board);
				if (win == false)
				{
					winChecker(board);
				}
				if (moveMade == true) // if a valid move is made generate a random number
				{
					randomNumberGen(board);
				}
				loseChecker(board);
				gameBoard.getChildren().clear(); // clear the gui game board
				guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
				if (lose == true)
				{
					// if the game is lost, create a message displaying that the player lost.

					Rectangle loseRect = new Rectangle(500, 200);
					loseRect.setFill(Color.rgb(255, 164, 152)); // change color to proper background color
					loseRect.setArcWidth(20);
					loseRect.setArcHeight(20);

					Text loseText = new Text("You lost :(");
					loseText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					loseText.setFill(Color.rgb(249, 245, 239));

					StackPane losePane = new StackPane();
					losePane.setLayoutY(200);
					losePane.setLayoutX(59);
					losePane.getChildren().addAll(loseRect, loseText);

					programBackground.getChildren().add(losePane);
					highScoreChecker();
					PauseTransition lossTimer = new PauseTransition(Duration.seconds(2));
					lossTimer.setOnFinished(event -> primaryStage.close());
					lossTimer.play();
				}
				score = (score + counter); // add scores made in move to running total
				if (moveMade == true)
				{
					moves = moves + 1;
				}
				scoreProperty.setValue(score);
				moveProperty.setValue(moves);
				counter = 0;
				break;
			}
			case LEFT:
			{
				leftAction(board);
				if (win == false)
				{
					winChecker(board);
				}
				if (moveMade == true) // if a valid move is made generate a random number
				{
					randomNumberGen(board);
				}
				loseChecker(board);
				gameBoard.getChildren().clear(); // clear the gui game board
				guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
				if (lose == true)
				{
					// if the game is lost, create a message displaying that the player lost.

					Rectangle loseRect = new Rectangle(500, 200);
					loseRect.setFill(Color.rgb(255, 164, 152)); // change color to proper background color
					loseRect.setArcWidth(20);
					loseRect.setArcHeight(20);

					Text loseText = new Text("You lost :(");
					loseText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					loseText.setFill(Color.rgb(249, 245, 239));

					StackPane losePane = new StackPane();
					losePane.setLayoutY(200);
					losePane.setLayoutX(59);
					losePane.getChildren().addAll(loseRect, loseText);

					programBackground.getChildren().add(losePane);
					highScoreChecker();
					PauseTransition lossTimer = new PauseTransition(Duration.seconds(2));
					lossTimer.setOnFinished(event -> primaryStage.close());
					lossTimer.play();
				}
				score = (score + counter); // add scores made in move to running total
				if (moveMade == true)
				{
					moves = moves + 1;
				}
				scoreProperty.setValue(score);
				moveProperty.setValue(moves);
				counter = 0;
				break;
			}
			case RIGHT:
			{
				rightAction(board);
				if (win == false)
				{
					winChecker(board);
				}
				if (moveMade == true) // if a valid move is made generate a random number
				{
					randomNumberGen(board);
				}
				loseChecker(board);
				gameBoard.getChildren().clear(); // clear the gui game board
				guiGameBoard.getChildren().add(generateGameBoard(board)); // generate the updated game board
				if (lose == true)
				{
					// if the game is lost, create a message displaying that the player lost.

					Rectangle loseRect = new Rectangle(500, 200);
					loseRect.setFill(Color.rgb(255, 164, 152)); // change color to proper background color
					loseRect.setArcWidth(20);
					loseRect.setArcHeight(20);

					Text loseText = new Text("You lost :(");
					loseText.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					loseText.setFill(Color.rgb(249, 245, 239));

					StackPane losePane = new StackPane();
					losePane.setLayoutY(200);
					losePane.setLayoutX(59);
					losePane.getChildren().addAll(loseRect, loseText);

					programBackground.getChildren().add(losePane);
					highScoreChecker();
					PauseTransition lossTimer = new PauseTransition(Duration.seconds(2));
					lossTimer.setOnFinished(event -> primaryStage.close());
					lossTimer.play();
				}
				score = (score + counter); // add scores made in move to running total
				if (moveMade == true)
				{
					moves = moves + 1;
				}
				scoreProperty.setValue(score);
				moveProperty.setValue(moves);
				counter = 0;
				break;
			}
			}
		});
	}

	public static void randomNumberGen(int[][] board)
	{
		//this method looks at all possible locations to place a 2 or 4 and associates each row and column into a list. A random number generator
		//is created that is the size of the list. A random number is chosen and the associated row and column receive a random 2 or 4 based on a
		//90 percent chance for 2 and 10 percent chance for 4 through another random number generator
		int x = 0;
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...
			{
				if (board[rows][columns] == 0) // if this is a possible location to place a number...
				{
					x++; // add 1 to the counter!
				}
			}
		}

		int[] zeroRows = new int[x]; // create an array for the row number with the size
		int[] zeroColumns = new int[x]; // create an array for the column number
		int indexCounter = 0; // an index counter will specify locations for the values inputed into these new
								// arrays
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (board[rows][columns] == 0) // if this is a possible location to place a number...
				{
					zeroRows[indexCounter] = rows; // input the row number into this array
					zeroColumns[indexCounter] = columns; // input the column number into this array
					indexCounter++; // increase index counter by one for next zero location
				}
			}
		}
		int randomZero = (int) (Math.random() * (x)); // determines location to place the two or four
		int fourOrTwo = (int) (Math.random() * (10)); // determines whether the number is a two or four
		if (fourOrTwo == 0)
		{
			board[zeroRows[randomZero]][zeroColumns[randomZero]] = 4;
		} else
		{
			board[zeroRows[randomZero]][zeroColumns[randomZero]] = 2;
		}
	}

	public static void upAction(int[][] board)

	{
		// modifies the array based on the direction inputted.
		// the first section packs the tiles together in the specified direction by
		// checking any possible
		// zeroes in the specified direction and then filling them until no more are
		// able to be filled
		// the second section makes combinations of similar number by checking for
		// nearby similar tiles
		// the number tile in the direction specified is doubled while the one below is
		// replaced by a zero.
		// since there is a possibliity of zero holes being created after combining, one
		// more pack operation is performed.

		boardUndo = boardCopy(board); // an undo copy of the board is saved in case of an undo
		moveMade = false; // used to verify that a valid move is made
		// TO PACK TILES TOGETHER
		for (int possMoves = 0; possMoves < 3; possMoves++) // total number of loops to ensure all numbers are packed
															// together in the specified direction
		{
			for (int rows = 0; rows < 4; rows++) // for every row...
			{
				for (int columns = 0; columns < 4; columns++) // for every column...

				{
					if (rows > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows - 1][columns] == 0) // if the spot in the direction specified is empty...
						{
							board[rows - 1][columns] = board[rows][columns]; // move the current index value that
																				// direction
							board[rows][columns] = 0; // replace the previous index value location with zero
							moveMade = true; // a valid move is made
						}
					}

				}
			}
		}
		// TO MAKE COMBINATIONS
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (rows > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows - 1][columns] == board[rows][columns]) // if the value in the direction specified is
																			// equal to the current value...
					{
						board[rows - 1][columns] = board[rows][columns] + board[rows - 1][columns]; // add the two
																									// values
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
						counter = counter + board[rows][columns] + board[rows - 1][columns]; // add up scores in this
																								// move
					}
				}

			}
		}
		// TO PACK TILES TOGETHER AFTER COMBINATION
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (rows > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows - 1][columns] == 0) // if the spot in the direction specified is empty...
					{
						board[rows - 1][columns] = board[rows][columns]; // move the current index value that direction
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
					}
				}

			}
		}
		if (moveMade == true)
		{
			howManyUndos++;
			undoSaveBoard(boardUndo);
		}

	}

	public static void downAction(int[][] board)
	{
		// modifies the array based on the direction inputted.
		// the first section packs the tiles together in the specified direction by
		// checking any possible
		// zeroes in the specified direction and then filling them until no more are
		// able to be filled
		// the second section makes combinations of similar number by checking for
		// nearby similar tiles
		// the number tile in the direction specified is doubled while the one below is
		// replaced by a zero.
		// since there is a possibliity of zero holes being created after combining, one
		// more pack operation is performed.
		boardUndo = boardCopy(board);
		moveMade = false;
		// TO PACK TILES TOGETHER
		for (int possMoves = 0; possMoves < 3; possMoves++) // total number of loops to ensure all numbers are packed
															// together in the specified direction
		{
			for (int rows = 0; rows < 4; rows++) // for every row...
			{
				for (int columns = 0; columns < 4; columns++) // for every column...

				{
					if (rows < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows + 1][columns] == 0) // if the spot in the direction specified is empty...
						{
							board[rows + 1][columns] = board[rows][columns]; // move the current index value that
																				// direction
							board[rows][columns] = 0; // replace the previous index value location with zero
							moveMade = true; // a valid move is made
						}
					}

				}
			}
		}

		// TO MAKE COMBINATIONS
		for (int rows = 3; rows > -1; rows--) // for every row...
		{
			for (int columns = 3; columns > -1; columns--) // for every column...

			{
				if (rows < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows + 1][columns] == board[rows][columns]) // if the value in the direction specified is
																			// equal to the current value...
					{
						board[rows + 1][columns] = board[rows][columns] + board[rows + 1][columns]; // add the two
																									// values
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
						counter = counter + board[rows][columns] + board[rows + 1][columns]; // add up scores in this
																								// move
					}
				}

			}
		}
		// TO PACK TILES TOGETHER AFTER COMBINATION
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (rows < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows + 1][columns] == 0) // if the spot in the direction specified is empty...
					{
						board[rows + 1][columns] = board[rows][columns]; // move the current index value that direction
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
					}
				}

			}
		}
		if (moveMade == true)
		{
			howManyUndos++;
			undoSaveBoard(boardUndo);
		}

	}

	public static void leftAction(int[][] board)
	{
		// modifies the array based on the direction inputted.
		// the first section packs the tiles together in the specified direction by
		// checking any possible
		// zeroes in the specified direction and then filling them until no more are
		// able to be filled
		// the second section makes combinations of similar number by checking for
		// nearby similar tiles
		// the number tile in the direction specified is doubled while the one below is
		// replaced by a zero.
		// since there is a possibliity of zero holes being created after combining, one
		// more pack operation is performed.
		boardUndo = boardCopy(board);
		moveMade = false; // used to verify that a valid move is made
		// TO PACK TILES TOGETHER
		for (int possMoves = 0; possMoves < 3; possMoves++) // total number of loops to ensure all numbers are packed
															// together in the specified direction
		{
			for (int rows = 0; rows < 4; rows++) // for every row...
			{
				for (int columns = 0; columns < 4; columns++) // for every column...

				{
					if (columns > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows][columns - 1] == 0) // if the spot in the direction specified is empty...
						{
							board[rows][columns - 1] = board[rows][columns]; // move the current index value that
																				// direction
							board[rows][columns] = 0; // replace the previous index value location with zero
							moveMade = true; // a valid move is made
						}
					}

				}
			}
		}
		// TO MAKE COMBINATIONS
		for (int columns = 0; columns < 4; columns++) // for every row...
		{
			for (int rows = 0; rows < 4; rows++) // for every column...

			{
				if (columns > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows][columns - 1] == board[rows][columns]) // if the value in the direction specified is
																			// equal to the current value...
					{
						board[rows][columns - 1] = board[rows][columns] + board[rows][columns - 1]; // add the two
																									// values
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
						counter = counter + board[rows][columns] + board[rows][columns - 1]; // add up scores in this
																								// move
					}
				}

			}
		}
		// TO PACK TILES TOGETHER AFTER COMBINATION
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (columns > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows][columns - 1] == 0) // if the spot in the direction specified is empty...
					{
						board[rows][columns - 1] = board[rows][columns]; // move the current index value that direction
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
					}
				}

			}
		}
		if (moveMade == true)
		{
			howManyUndos++;
			undoSaveBoard(boardUndo);
		}

	}

	public static void rightAction(int[][] board)
	{
		// modifies the array based on the direction inputted.
		// the first section packs the tiles together in the specified direction by
		// checking any possible
		// zeroes in the specified direction and then filling them until no more are
		// able to be filled
		// the second section makes combinations of similar number by checking for
		// nearby similar tiles
		// the number tile in the direction specified is doubled while the one below is
		// replaced by a zero.
		// since there is a possibliity of zero holes being created after combining, one
		// more pack operation is performed.
		boardUndo = boardCopy(board);
		moveMade = false; // used to verify that a valid move is made
		// TO PACK TILES TOGETHER
		for (int possMoves = 0; possMoves < 3; possMoves++) // total number of loops to ensure all numbers are packed
															// together in the specified direction
		{
			for (int rows = 0; rows < 4; rows++) // for every row...
			{
				for (int columns = 0; columns < 4; columns++) // for every column...

				{
					if (columns < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows][columns + 1] == 0) // if the spot in the direction specified is empty...
						{
							board[rows][columns + 1] = board[rows][columns]; // move the current index value that
																				// direction
							board[rows][columns] = 0; // replace the previous index value location with zero
							moveMade = true; // a valid move is made
						}
					}

				}
			}
		}
		// TO MAKE COMBINATIONS
		for (int columns = 3; columns > -1; columns--) // for every row...
		{
			for (int rows = 3; rows > -1; rows--) // for every column...

			{
				if (columns < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows][columns + 1] == board[rows][columns]) // if the value in the direction specified is
																			// equal to the current value...
					{
						board[rows][columns + 1] = board[rows][columns] + board[rows][columns + 1]; // add the two
																									// values
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
						counter = counter + board[rows][columns] + board[rows][columns + 1]; // add up scores in this
																								// move

					}
				}

			}
		}
		// TO PACK TILES TOGETHER AFTER COMBINATION
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (columns < 3 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
				{
					if (board[rows][columns + 1] == 0) // if the spot in the direction specified is empty...
					{
						board[rows][columns + 1] = board[rows][columns]; // move the current index value that direction
						board[rows][columns] = 0; // replace the previous index value location with zero
						moveMade = true; // a valid move is made
					}
				}

			}
		}
		if (moveMade == true)
		{
			howManyUndos++;
			undoSaveBoard(boardUndo);
		}
	}

	public static Pane generateGameBoard(int[][] board)
	{
		// the following section of code creates a game board through two for loops
		// based on the 2D array of the board
		// an empty rectangle (following empty tile format) is generated for each
		// possible tile position
		// tiles vary based on number, color and text placement
		Pane gameBoard = new Pane();

		int xLocation = 18;
		int yLocation = 18;
		for (int row = 0; row < 4; row++)
		{
			for (int col = 0; col < 4; col++)
			{
				Pane tileAndFont = new Pane();
				switch (board[row][col]) // switch statement to handle cases of different tile numbers
				{
				case 0:
				{
					Rectangle emptyTile = new Rectangle(132, 132);
					emptyTile.setFill(Color.rgb(199, 185, 174)); // change color to proper background color
					emptyTile.setArcWidth(20);
					emptyTile.setArcHeight(20);
					emptyTile.setX(xLocation);
					emptyTile.setY(yLocation);
					tileAndFont.getChildren().add(emptyTile);
					break;
				}

				case 2:
				{
					Rectangle twoTile = new Rectangle(132, 132);
					twoTile.setFill(Color.rgb(235, 224, 214)); // change color to proper background color
					twoTile.setArcWidth(20);
					twoTile.setArcHeight(20);
					twoTile.setX(xLocation);
					twoTile.setY(yLocation);
					Text number = new Text(xLocation + 48, yLocation + 93, "2");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(129, 115, 103));
					tileAndFont.getChildren().add(twoTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 4:
				{
					Rectangle fourTile = new Rectangle(132, 132);
					fourTile.setFill(Color.rgb(234, 219, 196)); // change color to proper background color
					fourTile.setArcWidth(20);
					fourTile.setArcHeight(20);
					fourTile.setX(xLocation);
					fourTile.setY(yLocation);
					Text number = new Text(xLocation + 48, yLocation + 93, "4");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(129, 115, 103));
					tileAndFont.getChildren().add(fourTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 8:
				{
					Rectangle eightTile = new Rectangle(132, 132);
					eightTile.setFill(Color.rgb(238, 166, 118)); // change color to proper background color
					eightTile.setArcWidth(20);
					eightTile.setArcHeight(20);
					eightTile.setX(xLocation);
					eightTile.setY(yLocation);
					Text number = new Text(xLocation + 48, yLocation + 93, "8");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(eightTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 16:
				{
					Rectangle sixteenTile = new Rectangle(132, 132);
					sixteenTile.setFill(Color.rgb(240, 136, 96)); // change color to proper background color
					sixteenTile.setArcWidth(20);
					sixteenTile.setArcHeight(20);
					sixteenTile.setX(xLocation);
					sixteenTile.setY(yLocation);
					Text number = new Text(xLocation + 20, yLocation + 93, "16");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(sixteenTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 32:
				{
					Rectangle thirtyTwoTile = new Rectangle(132, 132);
					thirtyTwoTile.setFill(Color.rgb(241, 110, 91)); // change color to proper background color
					thirtyTwoTile.setArcWidth(20);
					thirtyTwoTile.setArcHeight(20);
					thirtyTwoTile.setX(xLocation);
					thirtyTwoTile.setY(yLocation);
					Text number = new Text(xLocation + 20, yLocation + 93, "32");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(thirtyTwoTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 64:
				{
					Rectangle sixtyFourTile = new Rectangle(132, 132);
					sixtyFourTile.setFill(Color.rgb(241, 78, 61)); // change color to proper background color
					sixtyFourTile.setArcWidth(20);
					sixtyFourTile.setArcHeight(20);
					sixtyFourTile.setX(xLocation);
					sixtyFourTile.setY(yLocation);
					Text number = new Text(xLocation + 20, yLocation + 93, "64");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 75));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(sixtyFourTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 128:
				{
					Rectangle oneTwentyEightTile = new Rectangle(132, 132);
					oneTwentyEightTile.setFill(Color.rgb(234, 199, 115)); // change color to proper background color
					oneTwentyEightTile.setArcWidth(20);
					oneTwentyEightTile.setArcHeight(20);
					oneTwentyEightTile.setX(xLocation);
					oneTwentyEightTile.setY(yLocation);
					Text number = new Text(xLocation + 12, yLocation + 87, "128");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 60));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(oneTwentyEightTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 256:
				{
					Rectangle twoFiftySixTile = new Rectangle(132, 132);
					twoFiftySixTile.setFill(Color.rgb(233, 195, 100)); // change color to proper background color
					twoFiftySixTile.setArcWidth(20);
					twoFiftySixTile.setArcHeight(20);
					twoFiftySixTile.setX(xLocation);
					twoFiftySixTile.setY(yLocation);
					Text number = new Text(xLocation + 12, yLocation + 87, "256");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 60));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(twoFiftySixTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 512:
				{
					Rectangle fiveTwelveTile = new Rectangle(132, 132);
					fiveTwelveTile.setFill(Color.rgb(233, 191, 87)); // change color to proper background color
					fiveTwelveTile.setArcWidth(20);
					fiveTwelveTile.setArcHeight(20);
					fiveTwelveTile.setX(xLocation);
					fiveTwelveTile.setY(yLocation);
					Text number = new Text(xLocation + 12, yLocation + 87, "512");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 60));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(fiveTwelveTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 1024:
				{
					Rectangle tenTwentyFourTile = new Rectangle(132, 132);
					tenTwentyFourTile.setFill(Color.rgb(233, 187, 76)); // change color to proper background color
					tenTwentyFourTile.setArcWidth(20);
					tenTwentyFourTile.setArcHeight(20);
					tenTwentyFourTile.setX(xLocation);
					tenTwentyFourTile.setY(yLocation);
					Text number = new Text(xLocation + 7, yLocation + 87, "1024");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 50));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(tenTwentyFourTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 2048:
				{
					Rectangle twentyFourtyEightTile = new Rectangle(132, 132);
					twentyFourtyEightTile.setFill(Color.rgb(233, 184, 65)); // change color to proper background color
					twentyFourtyEightTile.setArcWidth(20);
					twentyFourtyEightTile.setArcHeight(20);
					twentyFourtyEightTile.setX(xLocation);
					twentyFourtyEightTile.setY(yLocation);
					Text number = new Text(xLocation + 7, yLocation + 87, "2048");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 50));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(twentyFourtyEightTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 4096:
				{
					Rectangle fourtyNinetySixTile = new Rectangle(132, 132);
					fourtyNinetySixTile.setFill(Color.rgb(56, 51, 47)); // change color to proper background color
					fourtyNinetySixTile.setArcWidth(20);
					fourtyNinetySixTile.setArcHeight(20);
					fourtyNinetySixTile.setX(xLocation);
					fourtyNinetySixTile.setY(yLocation);
					Text number = new Text(xLocation + 7, yLocation + 87, "4096");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 50));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(fourtyNinetySixTile);
					tileAndFont.getChildren().add(number);
					break;
				}

				case 8192:
				{
					Rectangle eightyOneNinetyTwoTile = new Rectangle(132, 132);
					eightyOneNinetyTwoTile.setFill(Color.rgb(56, 51, 47)); // change color to proper background color
					eightyOneNinetyTwoTile.setArcWidth(20);
					eightyOneNinetyTwoTile.setArcHeight(20);
					eightyOneNinetyTwoTile.setX(xLocation);
					eightyOneNinetyTwoTile.setY(yLocation);
					Text number = new Text(xLocation + 7, yLocation + 87, "8192");
					number.setFont(Font.font("Sans", FontWeight.BOLD, 50));
					number.setFill(Color.rgb(249, 245, 239));
					tileAndFont.getChildren().add(eightyOneNinetyTwoTile);
					tileAndFont.getChildren().add(number);
					break;
				}
				}

				gameBoard.getChildren().add(tileAndFont);
				xLocation = (xLocation + 150); //150 is added for the location of the next tile
			}
			yLocation = (yLocation + 150); //150 is added for the location of the next tile
			xLocation = 18; //reset for the next time through 
		}
		return gameBoard;
	}

	public static int[][] boardCopy(int[][] board)
	{
		// This method makes a copy of the board at its current state in case an undo is
		// requested.
		// A new array is created and the contents of the game board are copied to it.
		int[][] board2 = new int[4][4];
		for (int rows = 0; rows < 4; rows++) // this for loop prints a new line at the end of every row
		{

			for (int columns = 0; columns < 4; columns++) // this for loop prints the index values of every row
			{
				board2[rows][columns] = board[rows][columns]; // copy current board state for undo
			}
		}
		return board2;

	}

	public static void winChecker(int[][] board)
	{
		// checks each tile for 2048 then asks the user if they want to continue
		// or end the program

		for (int rows = 0; rows < 4; rows++) // this for loop prints a new line at the end of every row
		{
			for (int columns = 0; columns < 4; columns++) // this for loop prints the index values of every row
			{
				if (board[rows][columns] == 2048)
				{
					// if won, open a new windows asking whether to close or keeping playing
					Stage winner = new Stage(); // new window
					winner.setTitle("You won!");

					VBox textAndButtons = new VBox();
					Text winText = new Text("You won! Keep playing?");
					textAndButtons.setAlignment(Pos.CENTER);

					HBox winnerButtons = new HBox(10);
					Button btYes = new Button("Yes");
					Button btNo = new Button("No");
					winnerButtons.setAlignment(Pos.CENTER);

					winnerButtons.getChildren().addAll(btYes, btNo); // left: Yes right: No
					textAndButtons.getChildren().addAll(winText, winnerButtons); // Top: text Bottom: buttons

					Scene winnerScene = new Scene(textAndButtons, 300, 50);
					winner.setScene(winnerScene);
					winner.show();

					btYes.setOnAction(e -> // if yes
					{
						win = true; // stop checking for winner
						winner.close(); // close the winner window
					});

					btNo.setOnAction(e -> // if no
					{
						System.exit(0); // exit the program

					});

				}
			}
		}
	}

	public static void loseChecker(int[][] board)
	{
		// checks for a loss by first checking for a full board
		// then checks to see if a left or up move are possible, if not, game restarts
		// or program ends based on input

		int fullBoard = 0;
		for (int rows = 0; rows < 4; rows++) // for every row...
		{
			for (int columns = 0; columns < 4; columns++) // for every column...

			{
				if (board[rows][columns] != 0) // first check to see if the board is full
				{
					fullBoard++; // add one to counter
				}

			}
		}
		if (fullBoard == 16) // if the board is full...
		{
			boolean possibleMove = false;
			for (int rows = 0; rows < 4; rows++) // for every row...
			{
				for (int columns = 0; columns < 4; columns++) // for every column...

				{
					if (rows > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows - 1][columns] == board[rows][columns]) // if the value in the direction specified
																				// is equal to the current value...
						{
							possibleMove = true;
						}
					}

				}
			}
			for (int columns = 0; columns < 4; columns++) // for every row...
			{
				for (int rows = 0; rows < 4; rows++) // for every column...

				{
					if (columns > 0 & board[rows][columns] != 0) // to avoid an IndexOutOfBoundsException...
					{
						if (board[rows][columns - 1] == board[rows][columns]) // if the value in the direction specified
																				// is equal to the current value...
						{
							possibleMove = true;
						}
					}

				}
			}
			if (possibleMove == false)
			{
				lose = true;

			}
		}
	}

	public static void saveBoard(int[][] board)
	{
		//the first few lines are used to create place holders.
		//the new file input streams grab the current undo saves before outputting them and the board
		//as well as the score and moves to the 2048 save file
		int[][] undo1 = null;
		int[][] undo2 = null;
		int[][] undo3 = null;
		int[][] undo4 = null;
		int[][] undo5 = null;
		int[][] undo6 = null;
		int[][] undo7 = null;
		int[][] undo8 = null;
		int[][] undo9 = null;
		int[][] undo10 = null;
		try
		{
			FileInputStream fileInput1 = new FileInputStream("undo1.dat");
			ObjectInputStream objectInput1 = new ObjectInputStream(fileInput1);
			undo1 = (int[][]) objectInput1.readObject();
			
			FileInputStream fileInput2 = new FileInputStream("undo2.dat");
			ObjectInputStream objectInput2 = new ObjectInputStream(fileInput2);
			undo2 = (int[][]) objectInput2.readObject();
			
			FileInputStream fileInput3 = new FileInputStream("undo3.dat");
			ObjectInputStream objectInput3 = new ObjectInputStream(fileInput3);
			undo3 = (int[][]) objectInput3.readObject();
			
			FileInputStream fileInput4 = new FileInputStream("undo4.dat");
			ObjectInputStream objectInput4 = new ObjectInputStream(fileInput4);
			undo4 = (int[][]) objectInput4.readObject();
			
			FileInputStream fileInput5 = new FileInputStream("undo5.dat");
			ObjectInputStream objectInput5 = new ObjectInputStream(fileInput5);
			undo5 = (int[][]) objectInput5.readObject();
			
			FileInputStream fileInput6 = new FileInputStream("undo6.dat");
			ObjectInputStream objectInput6 = new ObjectInputStream(fileInput6);
			undo6 = (int[][]) objectInput6.readObject();
			
			FileInputStream fileInput7 = new FileInputStream("undo7.dat");
			ObjectInputStream objectInput7 = new ObjectInputStream(fileInput7);
			undo7 = (int[][]) objectInput7.readObject();
			
			FileInputStream fileInput8 = new FileInputStream("undo8.dat");
			ObjectInputStream objectInput8 = new ObjectInputStream(fileInput8);
			undo8 = (int[][]) objectInput8.readObject();
			
			FileInputStream fileInput9 = new FileInputStream("undo9.dat");
			ObjectInputStream objectInput9 = new ObjectInputStream(fileInput9);
			undo9 = (int[][]) objectInput9.readObject();
			
			FileInputStream fileInput10 = new FileInputStream("undo10.dat");
			ObjectInputStream objectInput10 = new ObjectInputStream(fileInput10);
			undo10 = (int[][]) objectInput10.readObject();
			
			FileOutputStream fileOutPut = new FileOutputStream("2048.dat");
			ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
			objectOutPut.writeObject(board);
			objectOutPut.writeObject(score);
			objectOutPut.writeObject(moves);
			objectOutPut.writeObject(undo1);
			objectOutPut.writeObject(undo2);
			objectOutPut.writeObject(undo3);
			objectOutPut.writeObject(undo4);
			objectOutPut.writeObject(undo5);
			objectOutPut.writeObject(undo6);
			objectOutPut.writeObject(undo7);
			objectOutPut.writeObject(undo8);
			objectOutPut.writeObject(undo9);
			objectOutPut.writeObject(undo10);
			
			
			
		} catch (Exception e)
		{

		}

	}

	public static void loadBoard(int[][] board)
	{ //the place holders in the first few lines are used to grab the undos from the save file to later put
	  //them in the undo saves from the undoSaveBoard method.
	  //the game board is also loaded into a place holder and then set equal for each row and column to the real game board
		int[][] undo1 = null;
		int[][] undo2 = null;
		int[][] undo3 = null;
		int[][] undo4 = null;
		int[][] undo5 = null;
		int[][] undo6 = null;
		int[][] undo7 = null;
		int[][] undo8 = null;
		int[][] undo9 = null;
		int[][] undo10 = null;
		int[][] newBoard = null;
		try
		{
			FileInputStream fileInput = new FileInputStream("2048.dat");
			ObjectInputStream objectInput = new ObjectInputStream(fileInput);
			newBoard = (int[][]) objectInput.readObject();

			for (int i = 0; i < 4; i++) // 4 rows...
			{
				for (int j = 0; j < 4; j++) // 4 columns...
				{
					board[i][j] = newBoard[i][j];
				}

			}
			score = (int) objectInput.readObject();
			moves = (int) objectInput.readObject();
			undo1 = (int[][]) objectInput.readObject();
			undo2 = (int[][]) objectInput.readObject();
			undo3 = (int[][]) objectInput.readObject();
			undo4 = (int[][]) objectInput.readObject();
			undo5 = (int[][]) objectInput.readObject();
			undo6 = (int[][]) objectInput.readObject();
			undo7 = (int[][]) objectInput.readObject();
			undo8 = (int[][]) objectInput.readObject();
			undo9 = (int[][]) objectInput.readObject();
			undo10 = (int[][]) objectInput.readObject();
			
			FileOutputStream fileOutPut1 = new FileOutputStream("undo1.dat");
			ObjectOutputStream objectOutPut1 = new ObjectOutputStream(fileOutPut1);
			objectOutPut1.writeObject(undo1);

			FileOutputStream fileOutPut2 = new FileOutputStream("undo2.dat");
			ObjectOutputStream objectOutPut2 = new ObjectOutputStream(fileOutPut2);
			objectOutPut2.writeObject(undo2);
			
			FileOutputStream fileOutPut3 = new FileOutputStream("undo3.dat");
			ObjectOutputStream objectOutPut3 = new ObjectOutputStream(fileOutPut3);
			objectOutPut3.writeObject(undo3);
			
			FileOutputStream fileOutPut4 = new FileOutputStream("undo4.dat");
			ObjectOutputStream objectOutPut4 = new ObjectOutputStream(fileOutPut4);
			objectOutPut4.writeObject(undo4);
			
			FileOutputStream fileOutPut5 = new FileOutputStream("undo5.dat");
			ObjectOutputStream objectOutPut5 = new ObjectOutputStream(fileOutPut5);
			objectOutPut5.writeObject(undo5);
			
			FileOutputStream fileOutPut6 = new FileOutputStream("undo6.dat");
			ObjectOutputStream objectOutPut6 = new ObjectOutputStream(fileOutPut6);
			objectOutPut6.writeObject(undo6);
			
			FileOutputStream fileOutPut7 = new FileOutputStream("undo7.dat");
			ObjectOutputStream objectOutPut7 = new ObjectOutputStream(fileOutPut7);
			objectOutPut7.writeObject(undo7);

			FileOutputStream fileOutPut8 = new FileOutputStream("undo8.dat");
			ObjectOutputStream objectOutPut8 = new ObjectOutputStream(fileOutPut8);
			objectOutPut8.writeObject(undo8);
			
			FileOutputStream fileOutPut9 = new FileOutputStream("undo9.dat");
			ObjectOutputStream objectOutPut9 = new ObjectOutputStream(fileOutPut9);
			objectOutPut9.writeObject(undo9);
			
			FileOutputStream fileOutPut10 = new FileOutputStream("undo10.dat");
			ObjectOutputStream objectOutPut10 = new ObjectOutputStream(fileOutPut10);
			objectOutPut10.writeObject(undo10);

			
		} catch (Exception e)
		{

		}

	}

	public static void undoSaveBoard(int[][] boardUndo)
	{
		//based on how many undos are so far allowed the undoBoard is placed into the most recent undo data file.
		//if an eleventh allowed undo state occurs, all undo boards are moved down one file to make room for the most recent.
		//then the undo counter is put back to 10 so it if it hits 11 again all the files will be moved down again.
		switch (howManyUndos)
		{
		case 1:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo1.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 2:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo2.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 3:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo3.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 4:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo4.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 5:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo5.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 6:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo6.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 7:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo7.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 8:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo8.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 9:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo9.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}

		case 10:
		{
			try
			{
				FileOutputStream fileOutPut = new FileOutputStream("undo10.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			break;
		}
		case 11:
		{
			int[][] BoardCopier = null;
			try
			{
				FileInputStream fileInput1 = new FileInputStream("undo2.dat");
				ObjectInputStream objectInput1 = new ObjectInputStream(fileInput1);
				BoardCopier = (int[][]) objectInput1.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo2.dat"));

				FileOutputStream fileOutPut1 = new FileOutputStream("undo1.dat");
				ObjectOutputStream objectOutPut1 = new ObjectOutputStream(fileOutPut1);
				objectOutPut1.writeObject(BoardCopier);

				FileInputStream fileInput2 = new FileInputStream("undo3.dat");
				ObjectInputStream objectInput2 = new ObjectInputStream(fileInput2);
				BoardCopier = (int[][]) objectInput2.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo3.dat"));

				FileOutputStream fileOutPut2 = new FileOutputStream("undo2.dat");
				ObjectOutputStream objectOutPut2 = new ObjectOutputStream(fileOutPut2);
				objectOutPut2.writeObject(BoardCopier);

				FileInputStream fileInput3 = new FileInputStream("undo4.dat");
				ObjectInputStream objectInput3 = new ObjectInputStream(fileInput3);
				BoardCopier = (int[][]) objectInput3.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo4.dat"));

				FileOutputStream fileOutPut3 = new FileOutputStream("undo3.dat");
				ObjectOutputStream objectOutPut3 = new ObjectOutputStream(fileOutPut3);
				objectOutPut3.writeObject(BoardCopier);

				FileInputStream fileInput4 = new FileInputStream("undo5.dat");
				ObjectInputStream objectInput4 = new ObjectInputStream(fileInput4);
				BoardCopier = (int[][]) objectInput4.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo5.dat"));

				FileOutputStream fileOutPut4 = new FileOutputStream("undo4.dat");
				ObjectOutputStream objectOutPut4 = new ObjectOutputStream(fileOutPut4);
				objectOutPut4.writeObject(BoardCopier);

				FileInputStream fileInput5 = new FileInputStream("undo6.dat");
				ObjectInputStream objectInput5 = new ObjectInputStream(fileInput5);
				BoardCopier = (int[][]) objectInput5.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo6.dat"));

				FileOutputStream fileOutPut5 = new FileOutputStream("undo5.dat");
				ObjectOutputStream objectOutPut5 = new ObjectOutputStream(fileOutPut5);
				objectOutPut5.writeObject(BoardCopier);

				FileInputStream fileInput6 = new FileInputStream("undo7.dat");
				ObjectInputStream objectInput6 = new ObjectInputStream(fileInput6);
				BoardCopier = (int[][]) objectInput6.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo7.dat"));

				FileOutputStream fileOutPut6 = new FileOutputStream("undo6.dat");
				ObjectOutputStream objectOutPut6 = new ObjectOutputStream(fileOutPut6);
				objectOutPut6.writeObject(BoardCopier);

				FileInputStream fileInput7 = new FileInputStream("undo8.dat");
				ObjectInputStream objectInput7 = new ObjectInputStream(fileInput7);
				BoardCopier = (int[][]) objectInput7.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo8.dat"));

				FileOutputStream fileOutPut7 = new FileOutputStream("undo7.dat");
				ObjectOutputStream objectOutPut7 = new ObjectOutputStream(fileOutPut7);
				objectOutPut7.writeObject(BoardCopier);

				FileInputStream fileInput8 = new FileInputStream("undo9.dat");
				ObjectInputStream objectInput8 = new ObjectInputStream(fileInput8);
				BoardCopier = (int[][]) objectInput6.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo9.dat"));

				FileOutputStream fileOutPut8 = new FileOutputStream("undo8.dat");
				ObjectOutputStream objectOutPut8 = new ObjectOutputStream(fileOutPut8);
				objectOutPut8.writeObject(BoardCopier);

				FileInputStream fileInput9 = new FileInputStream("undo10.dat");
				ObjectInputStream objectInput9 = new ObjectInputStream(fileInput9);
				BoardCopier = (int[][]) objectInput9.readObject();
				Files.deleteIfExists(Paths.get("G:\\1510-gill, balihaar\\2048GUI\\undo10.dat"));

				FileOutputStream fileOutPut9 = new FileOutputStream("undo9.dat");
				ObjectOutputStream objectOutPut9 = new ObjectOutputStream(fileOutPut9);
				objectOutPut9.writeObject(BoardCopier);

				FileOutputStream fileOutPut = new FileOutputStream("undo9.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(boardUndo);

			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		default:
			break;
		}
	}

	public static void undoLoadBoard(int[][] board)
	{
		//based on the undo counter, a respective file is read and imported into the current game board. A maximum of 10 undos can occur.
		//everytime one occurs, the counter goes down until it hits zero, when a undo will not be allowed until another move is made.
		int[][] newBoard = null;

		switch (howManyUndos)
		{
		case 1:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo1.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 2:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo2.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 3:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo3.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 4:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo4.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 5:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo5.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 6:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo6.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 7:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo7.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 8:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo8.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 9:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo9.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		case 10:
		{
			try
			{
				FileInputStream fileInput = new FileInputStream("undo10.dat");
				ObjectInputStream objectInput = new ObjectInputStream(fileInput);
				newBoard = (int[][]) objectInput.readObject();

				for (int i = 0; i < 4; i++) // 4 rows...
				{
					for (int j = 0; j < 4; j++) // 4 columns...
					{
						board[i][j] = newBoard[i][j];
					}

				}
			} catch (Exception e)
			{

			}
			howManyUndos--;
			break;
		}

		}
	}

	public static void highScoreChecker()
	{   //creates a high score place holder which is used to read the high score data file.
		//the score is then compared to the high score, if bigger, a window pops up displaying
		//the new high score, it plays a sound and then closes after 5 seconds.
		int highScore;
		try
		{
			FileInputStream fileInput = new FileInputStream("highScore.dat");
			ObjectInputStream objectInput = new ObjectInputStream(fileInput);
			highScore = (int) objectInput.readObject();

			if (score > highScore)
			{
				FileOutputStream fileOutPut = new FileOutputStream("highScore.dat");
				ObjectOutputStream objectOutPut = new ObjectOutputStream(fileOutPut);
				objectOutPut.writeObject(score);

				Stage newHighScore = new Stage(); // new window
				newHighScore.setTitle("New High Score");

				VBox textAndButtons = new VBox();
				Label scoreText = new Label("New high score: " + score);
				textAndButtons.getChildren().add(scoreText);
				textAndButtons.setAlignment(Pos.CENTER);
				scoreText.setWrapText(true);

				Scene helpScene = new Scene(textAndButtons, 300, 300);
				newHighScore.setScene(helpScene);
				newHighScore.show();
				AudioClip tada = new AudioClip("G:\\1510-gill, balihaar\\2048GUI\\tada.wav");
				tada.play();
				PauseTransition scoreTimer = new PauseTransition(Duration.seconds(5));
				scoreTimer.setOnFinished(event -> newHighScore.close());
				scoreTimer.setOnFinished(event -> tada.play());
			}

		} catch (Exception e)
		{

		}
	}
}
