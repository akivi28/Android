package itstep.learning;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class CalcActivity extends AppCompatActivity {

    private static final int maxDigits = 10;
    private TextView tvResult;
    private TextView tvHistory;

    private String zeroSign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        zeroSign = getString(R.string.calc_btn_digit_0);
        tvResult = findViewById(R.id.calc_tv_result);
        tvHistory = findViewById(R.id.calc_tv_history);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::btnClickC);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::btnClickCE);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::btnClickBackspace);
        findViewById(R.id.calc_btn_coma).setOnClickListener(this::btnClickDigit);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::btnClickPlus);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::btnClickMinus);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::btnClickMultiply);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::btnClickDivide);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::btnClickEqual);
        findViewById(R.id.calc_btn_pm).setOnClickListener(this::btnClickPm);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::btnClickPercent);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::btnClickInverse);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::btnClickSquare);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::btnClickSqrt);

        for (int i = 0; i < 10; i++) {
            String btnIdName = "calc_btn_digit_" + i;
            int btnId = getResources().getIdentifier(btnIdName,"id",getPackageName());
            findViewById(btnId).setOnClickListener(this::btnClickDigit);
        }
        btnClickC(null);
    }

    private void btnClickSqrt(View view){
        String resText = tvResult.getText().toString();
        double resValue = Double.parseDouble(resText);
        if(resValue < 0){
            Toast.makeText(this, R.string.calc_msg_sqrt_negative, Toast.LENGTH_SHORT).show();
            return;
        }
        resValue = Math.sqrt(resValue);

        if (resValue == (int) resValue) {
            resText = Integer.toString((int) resValue);
        } else {
            resText = Double.toString(resValue);
        }
        tvHistory.setText("sqrt(" + resText + ") =");
        tvResult.setText(resText);
    }

    private void btnClickSquare(View view){
        String resText = tvResult.getText().toString();
        double resValue = Double.parseDouble(resText);
        resValue = resValue * resValue;

        if (resValue == (int) resValue) {
            resText = Integer.toString((int) resValue);
        } else {
            resText = Double.toString(resValue);
        }
        tvHistory.setText("sqr(" + resText + ") =");
        tvResult.setText(resText);
    }

    private void btnClickInverse(View view){
        String resText = tvResult.getText().toString();
        double resValue = Double.parseDouble(resText);
        if(resValue == 0){
            Toast.makeText(this, R.string.calc_msg_divide_by_zero, Toast.LENGTH_SHORT).show();
            return;
        }
        resValue = 1/resValue;

        if (resValue == (int) resValue) {
            resText = Integer.toString((int) resValue);
        } else {
            resText = Double.toString(resValue);
        }
        tvHistory.setText("1/(" + resText + ") =");
        tvResult.setText(resText);

    }

    private void btnClickPercent(View view){
        List<String> historyList = List.of(tvHistory.getText().toString().split(" "));
        if(historyList.size() == 2){
            double first = Double.parseDouble(historyList.get(0));
            double second = Double.parseDouble(tvResult.getText().toString());
            double result = first / 100 * second;

            String historyText = tvHistory.getText().toString();
            historyText += Double.toString(result);
            tvHistory.setText(historyText);

            historyList = List.of(historyText.split(" "));

            result = equalFunc(historyList);
            if(Double.isNaN(result)){
                tvResult.setText(zeroSign);
                tvHistory.setText("");
                return;
            }
            String resText;
            if (result == (int) result) {
                resText = Integer.toString((int) result);
            } else {
                resText = Double.toString(result);
            }
            historyText += " = ";
            tvHistory.setText(historyText);
            tvResult.setText(resText);
        }
    }

    private void btnClickPm(View view){
        String resText = tvResult.getText().toString();
        double resValue = Double.parseDouble(resText);
        resValue = -resValue;

        if (resValue == (int) resValue) {
            resText = Integer.toString((int) resValue);
        } else {
            resText = Double.toString(resValue);
        }
        tvResult.setText(resText);
    }

    private void btnClickEqual(View view){
        String resText = tvResult.getText().toString();
        String historyText = tvHistory.getText().toString();
        historyText += resText;

        List<String> historyList = List.of(historyText.split(" "));
        if(historyList.size() == 3){
            double result = equalFunc(historyList);
            if(Double.isNaN(result)){
                tvResult.setText(zeroSign);
                tvHistory.setText("");
                return;
            }

            if (result == (int) result) {
                resText = Integer.toString((int) result);
            } else {
                resText = Double.toString(result);
            }
            historyText += " = ";
        }

        tvHistory.setText(historyText);
        tvResult.setText(resText);
    }

    private double equalFunc(List<String> historyList){
        double first = Double.parseDouble(historyList.get(0));
        String operation = historyList.get(1);
        double second = Double.parseDouble(historyList.get(2));

        double result = 0;
        if(getString(R.string.calc_btn_plus).equals(operation)){
            result = first + second;
        }
        else if(getString(R.string.calc_btn_minus).equals(operation)){
            result = first - second;
        }
        else if(getString(R.string.calc_btn_multiply).equals(operation)){
            result = first * second;
        }
        else if(getString(R.string.calc_btn_divide).equals(operation)){
            if(second == 0){
                Toast.makeText(this, R.string.calc_msg_divide_by_zero, Toast.LENGTH_SHORT).show();
                return Double.NaN;
            }
            result = first / second;

        }
        return result;
    }

    private void operationFunc(String operation){
        String resText = tvResult.getText().toString();
        String historyText = tvHistory.getText().toString();

        if(historyText.contains(getString(R.string.calc_btn_equal))){
            historyText = "";
        }

        historyText += resText;

        List<String> historyList = List.of(historyText.split(" "));
        if(historyList.size() == 3){
            double result = equalFunc(historyList);
            if(Double.isNaN(result)){
                tvResult.setText(zeroSign);
                tvHistory.setText("");
                return;
            }

            if (result == (int) result) {
                historyText = Integer.toString((int) result);
            } else {
                historyText = Double.toString(result);
            }
        }

        historyText += " " + operation + " ";

        tvHistory.setText(historyText);
        tvResult.setText(zeroSign);


    }

    private void btnClickPlus(View view){
        operationFunc(getString(R.string.calc_btn_plus));
    }

    private void btnClickMinus(View view){
        operationFunc(getString(R.string.calc_btn_minus));
    }

    private void btnClickMultiply(View view){
        operationFunc(getString(R.string.calc_btn_multiply));
    }

    private void btnClickDivide(View view){
        operationFunc(getString(R.string.calc_btn_divide));
    }
    private void btnClickBackspace(View view){
        String resText = tvResult.getText().toString();
        resText = resText.substring(0,resText.length()-1);
        if(resText.isEmpty()){
            resText = zeroSign;
        }
        tvResult.setText(resText);
    }

    private void btnClickDigit(View view){
        String resText = tvResult.getText().toString();
        String newText = ((Button)view).getText().toString();
        if(resText.equals(zeroSign) && newText.equals(getString(R.string.calc_btn_coma))){
            resText += newText;
        }
        else if(resText.equals(zeroSign)){
            resText = newText;
        }
        else {
            if(resText.length()>=maxDigits){
                Toast.makeText(this, R.string.calc_msg_too_long, Toast.LENGTH_SHORT).show();
                return;
            }
            if(resText.contains(getString(R.string.calc_btn_coma)) && newText.equals(getString(R.string.calc_btn_coma))){
                return;
            }
            resText += newText;
        }
        tvResult.setText(resText);
    }

    private void btnClickC(View view){
        tvResult.setText(R.string.calc_btn_digit_0);
        tvHistory.setText("");
    }

    private void btnClickCE(View view){
        tvResult.setText(R.string.calc_btn_digit_0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tv_result",tvResult.getText());
        outState.putCharSequence("tv_history",tvHistory.getText());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tv_result"));
        tvHistory.setText(savedInstanceState.getCharSequence("tv_history"));
    }
}