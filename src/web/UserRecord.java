package web;

public class UserRecord {

	private String accountName;
	private int day;
	private int actionCount;
	private String recordedAt;
	private String title;

	public UserRecord(String accountName, int day, int actionCount, String recordedAt, String title) {
		this.accountName = accountName;
		this.day = day;
		this.actionCount = actionCount;
		this.recordedAt = recordedAt;
		this.title = title;
	}

	public String getAccountName() {
		return accountName;
	}

	public int getDay() {
		return day;
	}

	public int getActionCount() {
		return actionCount;
	}

	public String getRecordedAt() {
		return recordedAt;
	}

	public String getTitle() {
		return title;
	}
}
