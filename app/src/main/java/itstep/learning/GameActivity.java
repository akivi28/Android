package itstep.learning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class GameActivity extends AppCompatActivity {
    private final String bestScoreFilename = "best_score.2048";
    private final int N = 4;
    private final int[][] cells = new int[N][N];
    private int[][] undo;
    private int prevScore;
    private final TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();

    private Animation spawnAnimation, collapseAnimation;
    private int score, bestScore;
    private TextView tvScore, tvBestScore;

    private Animation scaleDemo;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scaleDemo = AnimationUtils.loadAnimation(this, R.anim.scale_demo);

        spawnAnimation = AnimationUtils.loadAnimation(this,R.anim.game_spawn);
        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.game_collapse);

        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);

        LinearLayout gameField = findViewById(R.id.game_ll_field);
        gameField.post(()->{
            int vw = this.getWindow().getDecorView().getWidth();
            int fieldMargin = 20;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );
            layoutParams.setMargins(fieldMargin,fieldMargin,fieldMargin,fieldMargin);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);
        });


        gameField.setOnTouchListener( new OnSwipeListener(GameActivity.this) {
                    @Override
                    public void onSwipeBottom() {
                        if (canMoveBottom()) {
                            saveField();
                            moveBottom();
                            spawnCell();
                            showField();
                            checkLoss();
                        } else {
                            Toast.makeText(GameActivity.this, "No Down Move", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onSwipeLeft() {
                        if( canMoveLeft() ) {
                            saveField();
                            moveLeft();
                            spawnCell();
                            showField();
                            checkLoss();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onSwipeRight() {
                        if( canMoveRight() ) {
                            saveField();
                            moveRight();
                            spawnCell();
                            showField();
                            checkLoss();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onSwipeTop() {
                        if (canMoveTop()) {
                            saveField();
                            moveTop();
                            spawnCell();
                            showField();
                            checkLoss();
                        } else {
                            Toast.makeText(GameActivity.this, "No Up Move", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        initField();
        spawnCell();
        showField();

        findViewById(R.id.game_btn_new).setOnClickListener(this::newGameBtnClick);
        findViewById(R.id.game_btn_undo).setOnClickListener( v -> undoMove());
    }

    private void checkLoss(){
        if (!canMoveBottom() && !canMoveRight() && !canMoveTop() && !canMoveLeft()){
            new AlertDialog
                    .Builder(this, androidx.appcompat.R.style.Base_V7_ThemeOverlay_AppCompat_Dialog)
                    .setTitle("Завершення гри")
                    .setIcon(android.R.drawable.btn_star_big_on)
                    .setMessage("Гра завершена з рахунком: " + score)
                    .setPositiveButton("OK", (dlg, btn) -> {
                        initField();
                        spawnCell();
                        showField();
                    })
                    .setCancelable(false)
                    .show();

        }
    }

    private void saveField(){
        undo = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i],0,undo[i],0,N);
        }
        prevScore = score;
    }

    private void undoMove() {
        if( undo == null ) {
            showUndoMessage();
            return;
        }
        score = prevScore;
        for( int i = 0; i < N; i++ ) {
            System.arraycopy( undo[i], 0, cells[i], 0, N );
        }
        undo = null;
        showField();
    }

    private void showUndoMessage() {
        new AlertDialog
                .Builder(this, androidx.appcompat.R.style.Base_V7_ThemeOverlay_AppCompat_Dialog )
                .setTitle( "Обмеження" )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setMessage( "Скасування ходу неможливе" )
                .setNeutralButton( "Закрити", (dlg, btn) -> {} )
                .setPositiveButton( "Підписка", (dlg, btn) -> Toast.makeText(this, "Скоро буде", Toast.LENGTH_SHORT).show() )
                .setNegativeButton( "Вийти", (dlg, btn) -> finish() )
                .setCancelable( false )
                .show();
    }

    private void saveBestScore() {
        try( FileOutputStream fos = openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream writer = new DataOutputStream( fos )
        ) {
            writer.writeInt( bestScore );
            writer.flush();
        }
        catch( IOException ex ) {
            Log.e( "GameActivity::saveBestScore",
                    ex.getMessage() != null ?  ex.getMessage() : "Error writing file"
            ) ;
        }
    }

    private void loadBestScore() {
        try(FileInputStream fos = openFileInput( bestScoreFilename );
            DataInputStream reader = new DataInputStream( fos )
        ) {
            bestScore = reader.readInt();
        }
        catch( IOException ex ) {
            Log.e( "GameActivity::loadBestScore",
                    ex.getMessage() != null ?  ex.getMessage() : "Error writing file"
            ) ;
        }
    }

    private void newGameBtnClick(View view){
        initField();
        spawnCell();
        showField();

    }

    private boolean canMoveLeft() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if( cells[i][j] != 0 && ( cells[i][j-1] == 0 || cells[i][j-1] == cells[i][j] ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean canMoveRight() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (cells[i][j] != 0 && (cells[i][j + 1] == 0 || cells[i][j + 1] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean canMoveBottom() {
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N - 1; i++) {
                if (cells[i][j] != 0 && (cells[i + 1][j] == 0 || cells[i + 1][j] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean canMoveTop() {
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) {
                if (cells[i][j] != 0 && (cells[i - 1][j] == 0 || cells[i - 1][j] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveTop() {
        for (int j = 0; j < N; j++) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int i = 0; i < N - 1; i++) {
                    if (cells[i + 1][j] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i + 1][j];
                        cells[i + 1][j] = 0;
                        wasShift = true;
                    }
                }
            } while (wasShift);

            for (int i = 0; i < N - 1; i++) {
                if (cells[i][j] == cells[i + 1][j] && cells[i][j] != 0) {
                    cells[i][j] *= 2;
                    score += cells[i][j];
                    tvCells[i][j].setTag(collapseAnimation);

                    for (int k = i + 1; k < N - 1; k++) {
                        cells[k][j] = cells[k + 1][j];
                    }
                    cells[N - 1][j] = 0;
                }
            }
        }
    }

    private void moveBottom() {
        for (int j = 0; j < N; j++) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int i = N - 1; i > 0; i--) {
                    if (cells[i - 1][j] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i - 1][j];
                        cells[i - 1][j] = 0;
                        wasShift = true;
                    }
                }
            } while (wasShift);

            for (int i = N - 1; i > 0; i--) {
                if (cells[i][j] == cells[i - 1][j] && cells[i][j] != 0) {
                    cells[i][j] *= 2;
                    score += cells[i][j];
                    tvCells[i][j].setTag(collapseAnimation);

                    for (int k = i - 1; k > 0; k--) {
                        cells[k][j] = cells[k - 1][j];
                    }
                    cells[0][j] = 0;
                }
            }
        }
    }

    private void moveLeft() {
        for (int i = 0; i < N; i++) {      // [4 2 2 4]
            int j0 = -1;
            for (int j = 0; j < N; j++) {
                if( cells[i][j] != 0 ) {
                    if( j0 == -1 ) {
                        j0 = j;
                    }
                    else {
                        if( cells[i][j] == cells[i][j0] ) {  // collapse
                            cells[i][j] *= 2;
                            score += cells[i][j];
                            tvCells[i][j].setTag(collapseAnimation);
                            cells[i][j0] = 0;
                            j0 = -1;
                        }
                        else {
                            j0 = j;
                        }
                    }
                }
            }
            j0 = -1;
            for (int j = 0; j < N; j++) {
                if( cells[i][j] == 0 ) {   // [0 2 0 4] -> [2 4 0 0]
                    if( j0 == -1 ) {       // [0 0 0 2]     [0 0 2 2]
                        j0 = j;
                    }
                }
                else if( j0 != -1 ) {
                    cells[i][j0] = cells[i][j];
                    tvCells[i][j0].setTag(tvCells[i][j].getTag());
                    cells[i][j] = 0;
                    tvCells[i][j].setTag(null);
                    j0 += 1;
                }
            }
        }
    }

    private void moveRight() {
        for( int i = 0; i < N; i++ ) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int j = N - 1; j > 0; j--) {
                    if (cells[i][j - 1] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        wasShift = true;
                    }
                }
            } while( wasShift );

            for( int j = N - 1; j > 0; j-- ) {
                if( cells[i][j - 1] == cells[i][j] && cells[i][j] != 0 ) {
                    cells[i][j] *= 2;
                    score += cells[i][j];
                    tvCells[i][j].setTag( collapseAnimation );

                    for( int k = j - 1; k > 0; k-- ) {
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;
                }
            }
        }
    }

    private boolean spawnCell() {
        List<Coordinates> freeCells = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if( cells[i][j] == 0 ) {
                    freeCells.add( new Coordinates(i, j) );
                }
            }
        }
        if( freeCells.isEmpty() ) {
            return false;
        }
        Coordinates randomCoordinates = freeCells.get( random.nextInt( freeCells.size() ) );
        cells[randomCoordinates.x][randomCoordinates.y] =
                random.nextInt(10) == 0 ? 4 : 2;
        tvCells[randomCoordinates.x][randomCoordinates.y].setTag(spawnAnimation);
        return true;
    }

    static class Coordinates {
        int x, y;
        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private void initField(){
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
//                cells[i][j] = (int) Math.pow(2,i * N + j);
                cells[i][j] = 0;
                tvCells[i][j] = findViewById(getResources().getIdentifier(
                        "game_cell_"+i+j,
                        "id",
                        getPackageName()
                ));
            }
        }
        score = 0;
        loadBestScore();
    }

    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].getBackground().setColorFilter(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        cells[i][j] <= 2048
                                                ? "game_tile_" + cells[i][j]
                                                : "game_tile_other",
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        ),
                        PorterDuff.Mode.SRC_ATOP);
                tvCells[i][j].setTextColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048
                                        ? "game_text_" + cells[i][j]
                                        : "game_text_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) ) ;
                if( tvCells[i][j].getTag() instanceof Animation ) {
                    tvCells[i][j].startAnimation( (Animation) tvCells[i][j].getTag() );
                    tvCells[i][j].setTag( null );
                }
            }
        }
        tvScore.setText( getString( R.string.game_tv_score, String.valueOf( score ) ) );
        if( score > bestScore ) {
            bestScore = score;
            tvBestScore.startAnimation(scaleDemo);
            saveBestScore();
        }
        tvBestScore.setText( getString( R.string.game_tv_best, String.valueOf( bestScore ) ) );
    }

}