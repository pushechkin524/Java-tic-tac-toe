package com.example.tic_tac;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button[][] buttons = new Button[3][3];
    private boolean playerX = true; // true for player X, false for player O
    private int[][] board = new int[3][3]; // 0 = empty, 1 = X, 2 = O
    private TextView statusText, statisticsText;
    private SharedPreferences sharedPreferences;
    private int xWins = 0, oWins = 0, draws = 0;
    private boolean isDarkTheme = false; // Track theme
    private boolean isPlayingWithBot = false; // Flag for playing against bot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTheme(); // Load the theme from SharedPreferences
        setContentView(R.layout.activity_main);
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        statusText = findViewById(R.id.statusText);
        statisticsText = findViewById(R.id.statisticsText);
        Button restartButton = findViewById(R.id.restartButton);
        Button resetStatisticsButton = findViewById(R.id.resetStatisticsButton); // Кнопка сброса статистики
        Button themeSwitchButton = findViewById(R.id.themeSwitchButton); // Кнопка смены темы
        Button playWithBotButton = findViewById(R.id.playWithBotButton); // Кнопка для игры с ботом

        sharedPreferences = getSharedPreferences("GameStats", MODE_PRIVATE); // Загрузка статистики
        xWins = sharedPreferences.getInt("XWins", 0);
        oWins = sharedPreferences.getInt("OWins", 0);
        draws = sharedPreferences.getInt("Draws", 0);
        updateStatistics(); // Обновляем статистику

        // Инициализация кнопок и их обработчиков кликов
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                String buttonID = "button" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setTextColor(Color.BLACK); // установите цвет текста по умолчанию
                buttons[i][j].setOnClickListener(v -> onCellClicked(row, col));
            }
        }

        restartButton.setOnClickListener(v -> resetGame());
        resetStatisticsButton.setOnClickListener(v -> resetStatistics());
        themeSwitchButton.setOnClickListener(v -> switchTheme());
        playWithBotButton.setOnClickListener(v -> {
            isPlayingWithBot = true;
            resetGame();
        });

        updateButtonTextColor();
    }

    private void onCellClicked(int row, int col) {
        if (board[row][col] != 0) {
            return; // клетка уже занята
        }

        if (playerX) {
            buttons[row][col].setText("X");
            board[row][col] = 1;
        } else {
            buttons[row][col].setText("O");
            board[row][col] = 2;
        }

        if (checkWin()) {
            if (playerX) {
                xWins++;
                statusText.setText("Player X wins!");
            } else {
                oWins++;
                statusText.setText("Player O wins!");
            }
            updateStatistics();
            disableButtons(); // Отключаем все кнопки после победы
        } else if (isBoardFull()) {
            draws++;
            statusText.setText("It's a draw!");
            updateStatistics();
        } else {
            playerX = !playerX; // смена игрока
            statusText.setText((playerX ? "Player X" : "Player O") + "'s turn");
            if (isPlayingWithBot && !playerX) {
                botMove(); // Делаем ход бота, если сейчас ход бота
            }
        }
    }

    private void botMove() {
        // Бот делает случайный ход
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (board[row][col] != 0); // ищем пустую клетку

        buttons[row][col].setText("O");
        board[row][col] = 2;

        if (checkWin()) {
            oWins++;
            statusText.setText("Player O wins!");
            updateStatistics();
            disableButtons();
        } else if (isBoardFull()) {
            draws++;
            statusText.setText("It's a draw!");
            updateStatistics();
        } else {
            playerX = !playerX; // смена игрока
            statusText.setText("Player X's turn");
        }
    }

    private boolean checkWin() {
        // Проверка строк, столбцов и диагоналей
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true;
            }
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true;
            }
        }
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true;
        }
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateStatistics() {
        statisticsText.setText("X Wins: " + xWins + ", O Wins: " + oWins + ", Draws: " + draws);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("XWins", xWins);
        editor.putInt("OWins", oWins);
        editor.putInt("Draws", draws);
        editor.apply();
    }

    private void resetGame() {
        // Сброс значений поля
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
                buttons[i][j].setText("");
            }
        }
        playerX = true;
        statusText.setText("Player X's turn");
        enableButtons(); // Включаем кнопки снова
    }

    private void disableButtons() {
        // Отключение всех кнопок
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void enableButtons() {
        // Включение всех кнопок
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private void resetStatistics() {
        xWins = 0;
        oWins = 0;
        draws = 0;
        updateStatistics();
    }

    private void loadTheme() {
        sharedPreferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        if (isDarkTheme) {
            setTheme(android.R.style.Theme_Black);
        } else {
            setTheme(android.R.style.Theme_Light);
        }
    }

    private void switchTheme() {
        // Переключение темы
        isDarkTheme = !isDarkTheme;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isDarkTheme", isDarkTheme);
        editor.apply();
        recreate(); // Перезагрузка активности, чтобы применить новую тему
    }

    private void updateButtonTextColor() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (isDarkTheme) {
                    buttons[i][j].setTextColor(Color.WHITE);
                } else {
                    buttons[i][j].setTextColor(Color.BLACK);
                }
            }
        }
    }
}