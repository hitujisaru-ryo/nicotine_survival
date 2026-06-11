package web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

public class UserDataDAO {

	private static final Path USER_DATA_PATH = Path.of("userData.txt");
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public ArrayList<UserRecord> findAll() {
		ArrayList<UserRecord> records = new ArrayList<UserRecord>();

		if (!Files.exists(USER_DATA_PATH)) {
			return records;
		}

		try {
			ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(USER_DATA_PATH, StandardCharsets.UTF_8));

			for (String line : lines) {
				UserRecord record = parseLine(line);

				if (record != null) {
					records.add(record);
				}
			}
		} catch (IOException e) {
			return records;
		}

		records.sort(Comparator.comparingInt(UserRecord::getDay).reversed()
				.thenComparing(Comparator.comparingInt(UserRecord::getActionCount).reversed()));
		return records;
	}

	public void addRecord(String accountName, int day, int actionCount, String title) {
		String safeAccountName = accountName == null || accountName.isBlank()
				? "名無し"
				: accountName.replace(",", " ").trim();
		String safeTitle = title == null || title.isBlank()
				? "null"
				: title.replace(",", " ").trim();
		String recordedAt = LocalDateTime.now().format(FORMATTER);
		String line = safeAccountName + "," + day + "," + actionCount + "," + recordedAt + "," + safeTitle
				+ System.lineSeparator();

		try {
			Files.writeString(USER_DATA_PATH, line, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("ランキング保存に失敗しました: " + e.getMessage());
		}
	}

	private UserRecord parseLine(String line) {
		if (line == null || line.isBlank()) {
			return null;
		}

		String[] values = line.split(",", 5);

		if (values.length < 4) {
			return null;
		}

		try {
			String accountName = values[0].trim();
			int day = Integer.parseInt(values[1].trim());
			int actionCount = Integer.parseInt(values[2].trim());
			String recordedAt = values[3].trim();
			String title = values.length >= 5 ? values[4].trim() : "null";
			return new UserRecord(accountName, day, actionCount, recordedAt, title);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
