import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JLabel bombLabel = new JLabel();
    JLabel timerLabel = new JLabel("Timer: 0");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    TimerPanel timerPanel;

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0; //goal is to click all tiles except the ones containing mines
    boolean gameOver = false;

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight + 100); // Incremented height for buttons and timer
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper");
        textLabel.setOpaque(true);

        bombLabel.setFont(new Font("Arial", Font.BOLD, 25));
        bombLabel.setHorizontalAlignment(JLabel.CENTER);
        bombLabel.setText("Bombs: " + mineCount);
        bombLabel.setOpaque(true);

        textPanel.setLayout(new GridLayout(1, 2));
        textPanel.add(textLabel);
        textPanel.add(bombLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols)); //8x8
        frame.add(boardPanel);

        buttonPanel.setLayout(new FlowLayout());
        frame.add(buttonPanel, BorderLayout.SOUTH);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        buttonPanel.add(restartButton);

        timerPanel = new TimerPanel();
        buttonPanel.add(timerPanel);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        //left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText().equals("")) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        //right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().equals("") && tile.isEnabled()) {
                                tile.setText("🚩");
                                updateBombCount(-1);
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("");
                                updateBombCount(1);
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }

        frame.setVisible(true);

        setMines();
    }

    void setMines() {
        mineList = new ArrayList<MineTile>();

        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows); //0-7
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
            }
        }
    }

    void revealMines() {
        for (int i = 0; i < mineList.size(); i++) {
            MineTile tile = mineList.get(i);
            tile.setText("💣");
        }

        gameOver = true;
        textLabel.setText("Game Over!");
        timerPanel.stopTimer();
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked += 1;

        int minesFound = 0;

        //top 3
        minesFound += countMine(r - 1, c - 1);  //top left
        minesFound += countMine(r - 1, c);      //top
        minesFound += countMine(r - 1, c + 1);  //top right

        //left and right
        minesFound += countMine(r, c - 1);    //left
        minesFound += countMine(r, c + 1);    //right

        //bottom 3
        minesFound += countMine(r + 1, c - 1);  //bottom left
        minesFound += countMine(r + 1, c);      //bottom
        minesFound += countMine(r + 1, c + 1);  //bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");

            //top 3
            checkMine(r - 1, c - 1);    //top left
            checkMine(r - 1, c);        //top
            checkMine(r - 1, c + 1);    //top right

            //left and right
            checkMine(r, c - 1);        //left
            checkMine(r, c + 1);        //right

            //bottom 3
            checkMine(r + 1, c - 1);    //bottom left
            checkMine(r + 1, c);        //bottom
            checkMine(r + 1, c + 1);    //bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
            timerPanel.stopTimer();
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        return mineList.contains(board[r][c]) ? 1 : 0;
    }

    void updateBombCount(int increment) {
        mineCount += increment;
        bombLabel.setText("Bombs: " + mineCount);
    }

    void resetGame() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                tile.setEnabled(true);
                tile.setText("");
            }
        }

        mineCount = 10;
        setMines();
        tilesClicked = 0;
        gameOver = false;
        textLabel.setText("Minesweeper");
        updateBombCount(0); // Reset bomb count to original value
        timerPanel.resetTimer();
    }

    class TimerPanel extends JPanel {
        private Timer timer;
        private int seconds;

        TimerPanel() {
            seconds = 0;
            timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
            add(timerLabel);
            startTimer();
        }

        void startTimer() {
            timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    seconds++;
                    timerLabel.setText("Timer: " + seconds);
                }
            });
            timer.start();
        }

        void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }

        void resetTimer() {
            seconds = 0;
            timerLabel.setText("Timer: 0");
            startTimer();
        }
    }


}