package itstep.learning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.main_btn_calc).setOnClickListener(this::onCalcBtnClick);
        findViewById(R.id.main_btn_game).setOnClickListener(this::onGameBtnClick);
        findViewById(R.id.main_btn_anim).setOnClickListener(this::onAnimBtnClick);
        findViewById( R.id.main_btn_chat ).setOnClickListener( this::onChatButtonClick );
    }

    private void onCalcBtnClick(View view){
        Intent intent = new Intent(this, CalcActivity.class);
        startActivity(intent);
    }
    private void onGameBtnClick(View view){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
    private void onAnimBtnClick(View view){
        Intent intent = new Intent(this,AnimActivity.class);
        startActivity(intent);
    }
    private void onChatButtonClick( View view ) {
        Intent intent = new Intent( MainActivity.this, ChatActivity.class );
        startActivity( intent );
    }
}