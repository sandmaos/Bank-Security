package comp3911.cwk2;

public class Account {
  private String accountNumber;
  private String description;
  private Integer balance;

  public String getAccountNumber() { return accountNumber; }
  public String getDescription() { return description; }
  public Integer getBalance() { return balance; }

  public void setAccountNumber(String value) { accountNumber = value; }
  public void setDescription(String value) { description = value; }
  public void setBalance(Integer value) { balance = value; }
}
