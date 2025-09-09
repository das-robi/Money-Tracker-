package utils.export;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.devrobin.moneytracker.MVVM.DAO.AccountDAO;
import com.devrobin.moneytracker.MVVM.DAO.BudgetDAO;
import com.devrobin.moneytracker.MVVM.DAO.TransactionDao;
import com.devrobin.moneytracker.MVVM.Model.AccountModel;
import com.devrobin.moneytracker.MVVM.Model.BudgetModel;
import com.devrobin.moneytracker.MVVM.Model.TransactionModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {
    private ExportUtils() {}

    public static long[] resolveRange(String range, String year, String month) {
        long now = System.currentTimeMillis();

        // Simple ranges; for custom range integrate a date picker in UI later.
        if ("Yearly".equalsIgnoreCase(range)) {
            return rangeForYear(Integer.parseInt(year));
        }
        else if ("Monthly".equalsIgnoreCase(range)) {
            return rangeForMonth(Integer.parseInt(year), Integer.parseInt(month));
        }
        else if ("All Time".equalsIgnoreCase(range)) {
            return new long[]{0L, Long.MAX_VALUE};
        } else {
            // Default: current month
            Date d = new Date(now);
            SimpleDateFormat yf = new SimpleDateFormat("yyyy", Locale.getDefault());
            SimpleDateFormat mf = new SimpleDateFormat("MM", Locale.getDefault());
            return rangeForMonth(Integer.parseInt(yf.format(d)), Integer.parseInt(mf.format(d)));
        }
    }

    private static long[] rangeForYear(int y) {
        long start = toMillis(y, 1, 1);
        long end = toMillis(y, 12, 31) + 86399999L;
        return new long[]{start, end};
    }

    private static long[] rangeForMonth(int y, int m) {
        long start = toMillis(y, m, 1);
        int lastDay = 28;
        if (m==1||m==3||m==5||m==7||m==8||m==10||m==12) lastDay = 31; else if (m==4||m==6||m==9||m==11) lastDay = 30; else lastDay = isLeap(y) ? 29 : 28;
        long end = toMillis(y, m, lastDay) + 86399999L;
        return new long[]{start, end};
    }

    private static boolean isLeap(int y) { return (y%4==0 && y%100!=0) || (y%400==0); }

    private static long toMillis(int y, int m, int d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.YEAR, y);
        c.set(java.util.Calendar.MONTH, m-1);
        c.set(java.util.Calendar.DAY_OF_MONTH, d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static Uri export(Context ctx, String format, String scope, long start, long end,
                             TransactionDao tDao, AccountDAO aDao, BudgetDAO bDao) throws IOException {
        String filenameBase = "moneytracker_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String mime;
        String ext;
        String data;

        List<TransactionModel> transactions = new ArrayList<>();
        List<AccountModel> accounts = new ArrayList<>();
        List<BudgetModel> budgets = new ArrayList<>();

        if (scope.contains("Transactions") || scope.contains("All")) {
            transactions = tDao.getTransactionByDateRange(start, end);
        }
        // Always fetch accounts to resolve transaction currencies
        accounts = aDao.getAllAccountsSync();
        if (scope.contains("Budgets") || scope.contains("All")) {
            budgets = bDao.getAllBudgetsSync();
        }

        // Build accountId -> currency/name maps
        java.util.HashMap<Integer, String> accountIdToCurrency = new java.util.HashMap<>();
        java.util.HashMap<Integer, String> accountIdToName = new java.util.HashMap<>();
        for (AccountModel am : accounts) {
            accountIdToCurrency.put(am.getAccountId(), am.getCurrency());
            accountIdToName.put(am.getAccountId(), safe(am.getAccountName()));
        }

        if ("CSV".equalsIgnoreCase(format)) {
            mime = "text/csv";
            ext = ".csv";
            data = toCsv(transactions, scope, accountIdToName);
        } else if ("JSON".equalsIgnoreCase(format)) {
            mime = "application/json";
            ext = ".json";
            data = toJson(transactions, scope, accountIdToName);
        } else {
            mime = "application/pdf";
            ext = ".pdf";
            byte[] pdfBytes = buildPdfBytes(transactions, accounts, budgets, scope, accountIdToCurrency, accountIdToName);
            String filename = filenameBase + ext;
            return saveToDownloads(ctx, filename, mime, pdfBytes);
        }

        String filename = filenameBase + ext;
        return saveToDownloads(ctx, filename, mime, data.getBytes(StandardCharsets.UTF_8));
    }

    // Create a temporary file in cache for sharing only; does not persist to Downloads
    public static Uri exportForShare(Context ctx, String format, String scope, long start, long end,
                                     TransactionDao tDao, AccountDAO aDao, BudgetDAO bDao) throws IOException {
        String filenameBase = "moneytracker_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String mime;
        String ext;
        String data;

        List<TransactionModel> transactions = new ArrayList<>();
        List<AccountModel> accounts = new ArrayList<>();
        List<BudgetModel> budgets = new ArrayList<>();

        if (scope.contains("Transactions") || scope.contains("All")) {
            transactions = tDao.getTransactionByDateRange(start, end);
        }
        accounts = aDao.getAllAccountsSync();
        if (scope.contains("Budgets") || scope.contains("All")) {
            budgets = bDao.getAllBudgetsSync();
        }

        if ("CSV".equalsIgnoreCase(format)) {
            mime = "text/csv";
            ext = ".csv";
            data = toCsv(transactions, scope, buildAccountNameMap(accounts));
        } else if ("JSON".equalsIgnoreCase(format)) {
            mime = "application/json";
            ext = ".json";
            data = toJson(transactions, scope, buildAccountNameMap(accounts));
        } else {
            mime = "application/pdf";
            ext = ".pdf";
            byte[] pdfBytes = buildPdfBytes(transactions, accounts, budgets, scope, buildCurrencyMap(accounts), buildAccountNameMap(accounts));
            File cacheDir = new File(ctx.getCacheDir(), "exports");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File outFile = new File(cacheDir, filenameBase + ext);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(pdfBytes);
                fos.flush();
            }
            return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", outFile);
        }

        File cacheDir = new File(ctx.getCacheDir(), "exports");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        File outFile = new File(cacheDir, filenameBase + ext);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", outFile);
    }

    private static String toCsv(List<TransactionModel> t, String scope, java.util.Map<Integer, String> accountNames) {
        StringBuilder sb = new StringBuilder();
        if (scope.contains("Transactions") || scope.contains("All")) {
            sb.append("Date,Category,Account,Type,Amount,Notes\n");
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            for (TransactionModel x : t) {
                String date = df.format(x.getTransactionDate());
                String category = safe(x.getCategory());
                String account = accountNames.getOrDefault(x.getAccountId(), "");
                String type = safe(x.getType());
                String amount = String.format(java.util.Locale.getDefault(), "%.2f", x.getAmount());
                String notes = safe(x.getNote());
                sb.append(csv(date)).append(',')
                        .append(csv(category)).append(',')
                        .append(csv(account)).append(',')
                        .append(csv(type)).append(',')
                        .append(csv(amount)).append(',')
                        .append(csv(notes)).append('\n');
            }
        }
        return sb.toString();
    }

    private static java.util.Map<Integer, String> buildCurrencyMap(List<AccountModel> accounts) {
        java.util.HashMap<Integer, String> map = new java.util.HashMap<>();
        for (AccountModel am : accounts) map.put(am.getAccountId(), am.getCurrency());
        return map;
    }

    private static java.util.Map<Integer, String> buildAccountNameMap(List<AccountModel> accounts) {
        java.util.HashMap<Integer, String> map = new java.util.HashMap<>();
        for (AccountModel am : accounts) map.put(am.getAccountId(), safe(am.getAccountName()));
        return map;
    }

    private static byte[] buildPdfBytes(List<TransactionModel> t, List<AccountModel> a, List<BudgetModel> b, String scope,
                                        java.util.Map<Integer, String> accountCurrency,
                                        java.util.Map<Integer, String> accountNames) throws IOException {
        PdfDocument doc = new PdfDocument();
        Paint textPaint = new Paint();
        textPaint.setTextSize(11f);
        Paint titlePaint = new Paint(textPaint);
        titlePaint.setTextSize(16f);
        titlePaint.setFakeBoldText(true);
        Paint headerPaint = new Paint(textPaint);
        headerPaint.setFakeBoldText(true);
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);

        int pageWidth = 595;   // A4
        int pageHeight = 842;
        int margin = 24;
        int y;
        int rowHeight = 20;

        int[] colWidths = new int[6];
        int tableWidth = pageWidth - margin * 2;
        // Date, Category, Account, Type, Amount, Notes
        colWidths[0] = (int)(tableWidth * 0.15f);
        colWidths[1] = (int)(tableWidth * 0.18f);
        colWidths[2] = (int)(tableWidth * 0.20f);
        colWidths[3] = (int)(tableWidth * 0.12f);
        colWidths[4] = (int)(tableWidth * 0.12f);
        colWidths[5] = tableWidth - (colWidths[0]+colWidths[1]+colWidths[2]+colWidths[3]+colWidths[4]);

        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        int pageNum = 1;
        PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create());
        Canvas canvas = page.getCanvas();

        // Title
        y = margin + 10;
        canvas.drawText("Expense Tracker - Transactions Report", margin, y, titlePaint);
        y += 20;

        // Header row
        int x = margin;
        String[] headers = new String[]{"Date","Category","Account","Type","Amount","Notes"};
        for (int i = 0; i < headers.length; i++) {
            canvas.drawRect(x, y, x + colWidths[i], y + rowHeight, borderPaint);
            canvas.drawText(headers[i], x + 4, y + 14, headerPaint);
            x += colWidths[i];
        }
        y += rowHeight;

        if (scope.contains("Transactions") || scope.contains("All")) {
            for (TransactionModel tx : t) {
                String[] row = new String[]{
                        df.format(tx.getTransactionDate()),
                        safe(tx.getCategory()),
                        accountNames.getOrDefault(tx.getAccountId(), ""),
                        safe(tx.getType()),
                        String.format(java.util.Locale.getDefault(), "%.2f", tx.getAmount()),
                        safe(tx.getNote())
                };

                // Calculate required height with wrapping for Notes
                int currentRowHeight = rowHeight;
                // Wrap columns 1,2,5 by width
                int[] wrapCols = new int[]{1,2,5};
                for (int wc : wrapCols) {
                    java.util.List<String> parts = wrapLine(row[wc], textPaint, colWidths[wc]-8);
                    int h = parts.size() * rowHeight;
                    if (h > currentRowHeight) currentRowHeight = h;
                }

                if (y + currentRowHeight > pageHeight - margin) {
                    doc.finishPage(page);
                    pageNum++;
                    page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create());
                    canvas = page.getCanvas();
                    y = margin + 10;
                    canvas.drawText("Expense Tracker - Transactions Report", margin, y, titlePaint);
                    y += 20;
                    // redraw header
                    x = margin;
                    for (int i = 0; i < headers.length; i++) {
                        canvas.drawRect(x, y, x + colWidths[i], y + rowHeight, borderPaint);
                        canvas.drawText(headers[i], x + 4, y + 14, headerPaint);
                        x += colWidths[i];
                    }
                    y += rowHeight;
                }

                // Draw the row borders and text (with wrapping where needed)
                x = margin;
                for (int c = 0; c < headers.length; c++) {
                    canvas.drawRect(x, y, x + colWidths[c], y + currentRowHeight, borderPaint);
                    if (c == 4) {
                        // Amount right-aligned
                        float textWidth = textPaint.measureText(row[c]);
                        canvas.drawText(row[c], x + colWidths[c] - 4 - textWidth, y + 14, textPaint);
                    } else if (c == 0 || c == 3) {
                        canvas.drawText(row[c], x + 4, y + 14, textPaint);
                    } else {
                        // Wrapped text for Category/Account/Notes
                        int yy = y + 14;
                        for (String wl : wrapLine(row[c], textPaint, colWidths[c]-8)) {
                            canvas.drawText(wl, x + 4, yy, textPaint);
                            yy += rowHeight;
                        }
                    }
                    x += colWidths[c];
                }
                y += currentRowHeight;
            }
        }

        doc.finishPage(page);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        doc.writeTo(baos);
        doc.close();
        return baos.toByteArray();
    }

    private static java.util.List<String> wrapLine(String text, Paint paint, int maxWidth) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        if (text == null) { out.add(""); return out; }
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.length() == 0 ? w : line.toString() + " " + w;
            if (paint.measureText(candidate) > maxWidth) {
                if (line.length() > 0) out.add(line.toString());
                line.setLength(0);
                line.append(w);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        out.add(line.toString());
        return out;
    }

    private static String toJson(List<TransactionModel> t, String scope, java.util.Map<Integer, String> accountNames) {
        StringBuilder sb = new StringBuilder();
        if (scope.contains("Transactions") || scope.contains("All")) {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sb.append("[\n");
            for (int i = 0; i < t.size(); i++) {
                TransactionModel x = t.get(i);
                if (i>0) sb.append(",\n");
                String date = df.format(x.getTransactionDate());
                String category = escape(x.getCategory());
                String account = escape(accountNames.getOrDefault(x.getAccountId(), ""));
                String type = escape(x.getType());
                String notes = escape(x.getNote());
                String amount = String.format(java.util.Locale.getDefault(), "%.2f", x.getAmount());
                sb.append("  {\n")
                        .append("    \"date\": \"").append(date).append("\",\n")
                        .append("    \"category\": \"").append(category).append("\",\n")
                        .append("    \"account\": \"").append(account).append("\",\n")
                        .append("    \"type\": \"").append(type).append("\",\n")
                        .append("    \"amount\": ").append(amount).append(",\n")
                        .append("    \"notes\": \"").append(notes).append("\"\n")
                        .append("  }");
            }
            sb.append("\n]");
        } else {
            sb.append("[]");
        }
        return sb.toString();
    }

    private static String toPrettyText(List<TransactionModel> t, List<AccountModel> a, List<BudgetModel> b, String scope, java.util.Map<Integer, String> accountCurrency) {
        StringBuilder sb = new StringBuilder();
        sb.append("MoneyTracker Export\n\n");
        if (scope.contains("Transactions") || scope.contains("All")) {
            sb.append("== Transactions ==\n");
            for (TransactionModel x : t) {
                sb.append("#").append(x.getTransId()).append(" ")
                        .append(x.getType()).append(" ")
                        .append(x.getAmount()).append(" ")
                        .append(accountCurrency.getOrDefault(x.getAccountId(), "")).append(" | ")
                        .append("date:").append(x.getTransactionDate()).append(" | cat:")
                        .append(safe(x.getCategory())).append("\n");
            }
            sb.append('\n');
        }
        if (scope.contains("Accounts") || scope.contains("All")) {
            sb.append("== Accounts ==\n");
            for (AccountModel x : a) {
                sb.append("#").append(x.getAccountId()).append(" ")
                        .append(x.getAccountName()).append(" (")
                        .append(x.getCardType()).append(") ")
                        .append(x.getCurrency()).append(" | bal:")
                        .append(x.getBalance()).append("\n");
            }
            sb.append('\n');
        }
        if (scope.contains("Budgets") || scope.contains("All")) {
            sb.append("== Budgets ==\n");
            for (BudgetModel x : b) {
                sb.append("#").append(x.getBudgetId()).append(" ")
                        .append(x.getCategory()).append(" ")
                        .append(x.getBudgetType()).append(" amt:")
                        .append(x.getBudgetAmount()).append(" spent:")
                        .append(x.getSpentAmount()).append(" date:")
                        .append(x.getYear()).append("-")
                        .append(x.getMonth()).append("-")
                        .append(x.getDay()).append("\n");
            }
        }
        return sb.toString();
    }

    private static String safe(String s) { return s == null ? "" : s.replace(","," "); }
    private static String escape(String s) { return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\""); }
    private static String csv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains(" ");
        String v = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + v + "\"" : v;
    }

    private static Uri saveToDownloads(Context ctx, String filename, String mime, byte[] bytes) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
        values.put(MediaStore.Downloads.MIME_TYPE, mime);
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        ContentResolver resolver = ctx.getContentResolver();
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri item = resolver.insert(collection, values);
        if (item == null) throw new IOException("Cannot create file");
        try (OutputStream os = resolver.openOutputStream(item)) {
            if (os == null) throw new IOException("No stream");
            os.write(bytes);
            os.flush();
        }
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(item, values, null, null);
        return item;
    }

    public static void share(Context ctx, Uri file, String format) {
        String mime = "application/octet-stream";
        if ("CSV".equalsIgnoreCase(format)) mime = "text/csv";
        else if ("JSON".equalsIgnoreCase(format)) mime = "application/json";
        else if ("PDF".equalsIgnoreCase(format)) mime = "application/pdf";

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(mime);
        share.putExtra(Intent.EXTRA_STREAM, file);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ctx.startActivity(Intent.createChooser(share, "Share export"));
    }
}

