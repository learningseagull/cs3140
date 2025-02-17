package edu.virginia.sde.hw1;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Apportionment {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar Apportionment.jar <file> [<seats>] [--hamilton]");
            return;
        }

        String filename = args[0];
        int totalSeats = args.length > 1 && args[1].matches("\\d+") ? Integer.parseInt(args[1]) : 435;
        boolean useHamilton = args.length > 2 && args[2].equals("--hamilton");

        try {
            Map<String, Integer> populations = readPopulationData(filename);
            Map<String, Integer> apportionment = useHamilton ? hamiltonMethod(populations, totalSeats) : huntingtonHillMethod(populations, totalSeats);
            apportionment.forEach((state, seats) -> System.out.println(state + " " + seats));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static Map<String, Integer> readPopulationData(String filename) throws Exception {
        Map<String, Integer> populations = new HashMap<>();
        if (filename.endsWith(".csv")) {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String header = br.readLine();
                String[] columns = header.split(",");
                int stateIndex = -1, popIndex = -1;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].equalsIgnoreCase("State")) stateIndex = i;
                    if (columns[i].equalsIgnoreCase("Population")) popIndex = i;
                }
                if (stateIndex == -1 || popIndex == -1) throw new Exception("CSV must contain 'State' and 'Population' columns.");
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    try {
                        populations.put(values[stateIndex], Integer.parseInt(values[popIndex]));
                    } catch (Exception ignored) {}
                }
            }
        } else if (filename.endsWith(".xlsx")) {
            try (FileInputStream fis = new FileInputStream(new File(filename)); Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();
                Row headerRow = rowIterator.next();
                int stateIndex = -1, popIndex = -1;
                for (Cell cell : headerRow) {
                    if (cell.getStringCellValue().equalsIgnoreCase("State")) stateIndex = cell.getColumnIndex();
                    if (cell.getStringCellValue().equalsIgnoreCase("Population")) popIndex = cell.getColumnIndex();
                }
                if (stateIndex == -1 || popIndex == -1) throw new Exception("Excel file must contain 'State' and 'Population' columns.");
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    try {
                        populations.put(row.getCell(stateIndex).getStringCellValue(), (int) row.getCell(popIndex).getNumericCellValue());
                    } catch (Exception ignored) {}
                }
            }
        } else {
            throw new Exception("Unsupported file format.");
        }
        return populations;
    }

    private static Map<String, Integer> huntingtonHillMethod(Map<String, Integer> populations, int totalSeats) {
        Map<String, Integer> seats = new HashMap<>();
        populations.forEach((state, pop) -> seats.put(state, 1));
        int remainingSeats = totalSeats - populations.size();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
        while (remainingSeats-- > 0) {
            pq.clear();
            for (Map.Entry<String, Integer> entry : seats.entrySet()) {
                double priority = entry.getValue() * Math.sqrt(entry.getValue() + 1);
                pq.add(new AbstractMap.SimpleEntry<>(entry.getKey(), populations.get(entry.getKey()) / priority));
            }
            String topState = pq.poll().getKey();
            seats.put(topState, seats.get(topState) + 1);
        }
        return seats;
    }

    private static Map<String, Integer> hamiltonMethod(Map<String, Integer> populations, int totalSeats) {
        Map<String, Integer> seats = new HashMap<>();
        double totalPopulation = populations.values().stream().mapToDouble(Integer::doubleValue).sum();
        double quota = totalPopulation / totalSeats;
        populations.forEach((state, pop) -> seats.put(state, (int) (pop / quota)));
        int assignedSeats = seats.values().stream().mapToInt(Integer::intValue).sum();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
        populations.forEach((state, pop) -> pq.add(new AbstractMap.SimpleEntry<>(state, pop / quota - seats.get(state))));
        while (assignedSeats < totalSeats) {
            String topState = pq.poll().getKey();
            seats.put(topState, seats.get(topState) + 1);
            assignedSeats++;
        }
        return seats;
    }
}
