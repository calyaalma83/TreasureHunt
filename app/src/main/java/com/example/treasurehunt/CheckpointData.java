package com.example.treasurehunt;

import java.util.ArrayList;
import java.util.Random;

public class CheckpointData {

    // Radius dalam meter untuk generate checkpoint di sekitar user
    private static final double MIN_RADIUS = 30;  // Minimal 30 meter dari user
    private static final double MAX_RADIUS = 100; // Maksimal 100 meter dari user

    // Array nama dan clue untuk checkpoint
    private static final String[] CHECKPOINT_NAMES = {
            "CHECKPOINT 1 - Titik Alpha",
            "CHECKPOINT 2 - Titik Bravo",
            "CHECKPOINT 3 - Titik Charlie",
            "CHECKPOINT 4 - Titik Delta",
            "CHECKPOINT 5 - Titik Echo"
    };

    private static final String[] CHECKPOINT_CLUES = {
            "Pergilah ke arah utara, target pertamamu menunggu di sana!",
            "Cari area terbuka di sekitarmu, checkpoint kedua tidak jauh!",
            "Berjalanlah ke arah matahari terbit, kamu semakin dekat!",
            "Hampir sampai! Cek area di sekitar bangunan terdekat.",
            "Finish line! Temukan titik terakhir untuk menyelesaikan misi!"
    };

    /**
     * Generate checkpoint secara otomatis di sekitar lokasi user
     * @param userLat - Latitude lokasi user saat ini
     * @param userLng - Longitude lokasi user saat ini
     * @return ArrayList berisi 5 checkpoint di sekitar user
     */
    public static ArrayList<Checkpoint> generateCheckpointsAroundUser(double userLat, double userLng) {
        ArrayList<Checkpoint> checkpoints = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            // Random jarak antara MIN_RADIUS dan MAX_RADIUS meter
            double distance = MIN_RADIUS + (random.nextDouble() * (MAX_RADIUS - MIN_RADIUS));

            // Random sudut 0-360 derajat (dalam radian)
            // Bagi area menjadi 5 bagian supaya checkpoint tersebar merata
            double baseAngle = (i * 72) + random.nextInt(60); // 72 = 360/5
            double angleRad = Math.toRadians(baseAngle);

            // Hitung koordinat baru berdasarkan jarak dan sudut
            double[] newCoords = calculateNewCoordinates(userLat, userLng, distance, angleRad);

            checkpoints.add(new Checkpoint(
                    CHECKPOINT_NAMES[i],
                    CHECKPOINT_CLUES[i],
                    newCoords[0], // latitude
                    newCoords[1]  // longitude
            ));
        }

        return checkpoints;
    }

    /**
     * Menghitung koordinat baru berdasarkan titik awal, jarak, dan sudut
     * @param lat - Latitude awal
     * @param lng - Longitude awal
     * @param distanceMeters - Jarak dalam meter
     * @param bearingRad - Sudut dalam radian
     * @return Array [newLat, newLng]
     */
    private static double[] calculateNewCoordinates(double lat, double lng, double distanceMeters, double bearingRad) {
        // Radius bumi dalam meter
        double earthRadius = 6371000;

        // Konversi latitude ke radian
        double latRad = Math.toRadians(lat);
        double lngRad = Math.toRadians(lng);

        // Hitung latitude baru
        double newLatRad = Math.asin(
                Math.sin(latRad) * Math.cos(distanceMeters / earthRadius) +
                        Math.cos(latRad) * Math.sin(distanceMeters / earthRadius) * Math.cos(bearingRad)
        );

        // Hitung longitude baru
        double newLngRad = lngRad + Math.atan2(
                Math.sin(bearingRad) * Math.sin(distanceMeters / earthRadius) * Math.cos(latRad),
                Math.cos(distanceMeters / earthRadius) - Math.sin(latRad) * Math.sin(newLatRad)
        );

        // Konversi kembali ke derajat
        double newLat = Math.toDegrees(newLatRad);
        double newLng = Math.toDegrees(newLngRad);

        return new double[]{newLat, newLng};
    }

    /**
     * Method lama untuk checkpoint manual (masih bisa dipakai kalau mau)
     */
    public static ArrayList<Checkpoint> getCheckpoints() {
        ArrayList<Checkpoint> checkpoints = new ArrayList<>();

        checkpoints.add(new Checkpoint(
                "CHECKPOINT 1 - Gerbang Utama",
                "Temukan gerbang besar tempat semua orang masuk.",
                -7.5755, 110.8243
        ));

        checkpoints.add(new Checkpoint(
                "CHECKPOINT 2 - Taman Tengah",
                "Cari area hijau dengan banyak pohon.",
                -7.5760, 110.8250
        ));

        checkpoints.add(new Checkpoint(
                "CHECKPOINT 3 - Perpustakaan",
                "Tempat di mana ilmu tersimpan dalam ribuan buku.",
                -7.5765, 110.8238
        ));

        checkpoints.add(new Checkpoint(
                "CHECKPOINT 4 - Kantin",
                "Tempat ramai saat jam makan.",
                -7.5770, 110.8255
        ));

        checkpoints.add(new Checkpoint(
                "CHECKPOINT 5 - Lapangan",
                "Area terbuka untuk upacara dan olahraga.",
                -7.5775, 110.8248
        ));

        return checkpoints;
    }

    // Helper methods
    public static void resetAllCheckpoints(ArrayList<Checkpoint> checkpoints) {
        for (Checkpoint cp : checkpoints) {
            cp.setCompleted(false);
        }
    }

    public static int getCompletedCount(ArrayList<Checkpoint> checkpoints) {
        int count = 0;
        for (Checkpoint cp : checkpoints) {
            if (cp.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public static boolean isAllCompleted(ArrayList<Checkpoint> checkpoints) {
        for (Checkpoint cp : checkpoints) {
            if (!cp.isCompleted()) {
                return false;
            }
        }
        return true;
    }
}