package com.devrobin.moneytracker.Views.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.ActivityExportSettingsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.NotificationHelper;
import utils.export.ExportUtils;

public class ExportSettingsActivity extends AppCompatActivity {

    ActivityExportSettingsBinding exportBinding;

    private TransactionDao transactionDao;
    private AccountDAO accountDao;
    private BudgetDAO budgetDao;

    private ExecutorService exportExecutor = Executors.newSingleThreadExecutor();
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        exportBinding = ActivityExportSettingsBinding.inflate(getLayoutInflater());
        setContentView(exportBinding.getRoot());


        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(this, R.array.export_formats, android.R.layout.simple_spinner_item);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportBinding.spinnerFormat.setAdapter(formatAdapter);

        ArrayAdapter<CharSequence> rangeAdapter = ArrayAdapter.createFromResource(this, R.array.export_ranges, android.R.layout.simple_spinner_item);
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportBinding.spinnerRange.setAdapter(rangeAdapter);

        ArrayAdapter<CharSequence> scopeAdapter = ArrayAdapter.createFromResource(this, R.array.export_scopes, android.R.layout.simple_spinner_item);
        scopeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportBinding.spinnerScope.setAdapter(scopeAdapter);


        // Year/Month spinners default to current
        int year = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int y = year; y >= year - 10; y--) years.add(String.valueOf(y));
        exportBinding.spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years));
        exportBinding.spinnerYear.setSelection(0);

        String[] months = new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"};
        exportBinding.spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months));
        exportBinding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        TransactionDatabase db = TransactionDatabase.getInstance(this);
        transactionDao = db.transDao();
        accountDao = db.accountDao();
        budgetDao = db.budgetDao();

        exportBinding.btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performExport(false);
            }
        });
        exportBinding.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performExport(true);
            }
        });

    }

    private void performExport(boolean shareAfter) {
        String format = exportBinding.spinnerFormat.getSelectedItem().toString();
        String range = exportBinding.spinnerRange.getSelectedItem().toString();
        String scope = exportBinding.spinnerScope.getSelectedItem().toString();
        String year = (String) exportBinding.spinnerYear.getSelectedItem();
        String month = (String) exportBinding.spinnerMonth.getSelectedItem();

        long[] startEnd = ExportUtils.resolveRange(range, year, month);
        long start = startEnd[0];
        long end = startEnd[1];

        exportBinding.btnExport.setEnabled(false);
        exportBinding.btnShare.setEnabled(false);

        exportExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Uri fileUri = ExportUtils.export(ExportSettingsActivity.this, format, scope, start, end, transactionDao, accountDao, budgetDao);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exportBinding.btnExport.setEnabled(true);
                            exportBinding.btnShare.setEnabled(true);
                            if (fileUri == null) {
                                Toast.makeText(ExportSettingsActivity.this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(ExportSettingsActivity.this, R.string.export_saved, Toast.LENGTH_SHORT).show();
                            if (notificationHelper != null) {
                                String title = getString(R.string.app_name) + " - Export Complete";
                                String msg = getString(R.string.export_saved);
                                notificationHelper.sendNotification(title, msg, 7001);
                            }
                            if (shareAfter) ExportUtils.share(ExportSettingsActivity.this, fileUri, format);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exportBinding.btnExport.setEnabled(true);
                            exportBinding.btnShare.setEnabled(true);
                            Toast.makeText(ExportSettingsActivity.this, getString(R.string.export_failed_with_reason, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    private void performShare() {
        final String format = exportBinding.spinnerFormat.getSelectedItem().toString();
        final String range = exportBinding.spinnerRange.getSelectedItem().toString();
        final String scope = exportBinding.spinnerScope.getSelectedItem().toString();
        final String year = (String) exportBinding.spinnerYear.getSelectedItem();
        final String month = (String) exportBinding.spinnerMonth.getSelectedItem();

        long[] startEnd = ExportUtils.resolveRange(range, year, month);
        final long start = startEnd[0];
        final long end = startEnd[1];

        exportBinding.btnExport.setEnabled(false);
        exportBinding.btnShare.setEnabled(false);

        exportExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final android.net.Uri shareUri = ExportUtils.exportForShare(ExportSettingsActivity.this, format, scope, start, end, transactionDao, accountDao, budgetDao);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exportBinding.btnExport.setEnabled(true);
                            exportBinding.btnShare.setEnabled(true);
                            if (shareUri == null) {
                                Toast.makeText(ExportSettingsActivity.this, R.string.export_failed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Directly open share sheet without persisting to Downloads
                            ExportUtils.share(ExportSettingsActivity.this, shareUri, format);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exportBinding.btnExport.setEnabled(true);
                            exportBinding.btnShare.setEnabled(true);
                            Toast.makeText(ExportSettingsActivity.this, getString(R.string.export_failed_with_reason, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exportExecutor != null) {
            exportExecutor.shutdown();
        }
    }

}