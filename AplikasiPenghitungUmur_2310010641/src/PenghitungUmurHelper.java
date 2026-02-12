import java.time.LocalDate;
import java.time.Period;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;
import javax.swing.JTextArea;
import org.json.JSONArray;
import org.json.JSONObject;

public class PenghitungUmurHelper {

    // Menghitung umur secara detail (tahun, bulan, hari)
    public String hitungUmurDetail(LocalDate lahir, LocalDate sekarang) {
        Period period = Period.between(lahir, sekarang);
        return period.getYears() + " tahun, " + period.getMonths() + " bulan, " + period.getDays() + " hari";
    }

    // Menghitung hari ulang tahun berikutnya
    public LocalDate hariUlangTahunBerikutnya(LocalDate lahir, LocalDate sekarang) {
        LocalDate ulangTahunBerikutnya = lahir.withYear(sekarang.getYear());
        if (!ulangTahunBerikutnya.isAfter(sekarang)) {
            ulangTahunBerikutnya = ulangTahunBerikutnya.plusYears(1);
        }
        return ulangTahunBerikutnya;
    }

    // Menerjemahkan teks hari ke bahasa Indonesia
    public String getDayOfWeekInIndonesian(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return "";
        }
    }
    // Mendapatkan peristiwa penting secara baris per baris
public void getPeristiwaBarisPerBaris(
        LocalDate tanggal,
        JTextArea txtAreaPeristiwa,
        Supplier<Boolean> shouldStop
) {
    try {
        // Cek jika proses diminta berhenti sebelum mulai
        if (shouldStop.get()) return;

        String urlString = "https://byabbe.se/on-this-day/"
                + tanggal.getMonthValue() + "/"
                + tanggal.getDayOfMonth() + "/events.json";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP response code: " + responseCode
                    + ". Silakan coba lagi nanti atau cek koneksi internet.");
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                // Cek jika proses diminta berhenti saat membaca data
                if (shouldStop.get()) {
                    conn.disconnect();
                    javax.swing.SwingUtilities.invokeLater(() ->
                            txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n")
                    );
                    return;
                }

                content.append(inputLine);
            }
        } finally {
            conn.disconnect();
        }

        JSONObject json = new JSONObject(content.toString());
        JSONArray events = json.getJSONArray("events");

        if (events.length() == 0) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    txtAreaPeristiwa.setText("Tidak ada peristiwa penting yang ditemukan pada tanggal ini.")
            );
            return;
        }

        // (Opsional) bersihkan dulu area output sebelum menambahkan
        javax.swing.SwingUtilities.invokeLater(() -> txtAreaPeristiwa.setText(""));

        for (int i = 0; i < events.length(); i++) {

            // Cek jika proses diminta berhenti sebelum memproses data
            if (shouldStop.get()) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n")
                );
                return;
            }

            JSONObject event = events.getJSONObject(i);
            String year = event.optString("year", "-");
            String description = event.optString("description", "-");

            String peristiwa = year + ": " + description;

            javax.swing.SwingUtilities.invokeLater(() ->
                    txtAreaPeristiwa.append(peristiwa + "\n")
            );
        }

    } catch (Exception e) {
        javax.swing.SwingUtilities.invokeLater(() ->
                txtAreaPeristiwa.setText("Gagal mendapatkan data peristiwa: " + e.getMessage())
        );
    }
}
}
