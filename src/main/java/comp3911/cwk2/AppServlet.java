package comp3911.cwk2;

import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

  private static final String AUTH_QUERY = "select * from account_owner where username=? and password=?";
  private static final String ACCOUNT_QUERY = "select * from account where owner_id=?";
  private static final String UPDATE = "UPDATE account_owner SET password=? where id=?";

  private final Configuration fm = new Configuration(Configuration.VERSION_2_3_28);
  private Connection database;

  @Override
  public void init() throws ServletException {
    configureTemplateEngine();
    connectToDatabase();
  }

  private void configureTemplateEngine() throws ServletException {
    try {
      fm.setDirectoryForTemplateLoading(new File("./templates"));
      fm.setDefaultEncoding("UTF-8");
      fm.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      fm.setLogTemplateExceptions(false);
      fm.setWrapUncheckedExceptions(true);
    }
    catch (IOException error) {
      throw new ServletException(error.getMessage());
    }
  }

  private void connectToDatabase() throws ServletException {
    try {
      Decryptor decryptor = new Decryptor();
      decryptor.readData();
      String dbFile = decryptor.decryptData();
      String url = "jdbc:sqlite:" + dbFile;
      database = DriverManager.getConnection(url);

      Statement stmt=database.createStatement();
      ResultSet rs=stmt.executeQuery("select * from account_owner");
      int i = 0;
while(rs.next())
{
  i++;
int index = rs.getInt("id");
String str0 = rs.getString("password");
String str1 = MD5Util.getMD5String(str0);
PreparedStatement Sm = database.prepareStatement(UPDATE);
String num = Integer.toString(index);
Sm.setString(1, str1);
Sm.setInt(2, i);
Sm.execute();
}
    }
    catch (Exception error) {
      throw new ServletException(error.getMessage());
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
    try {
      Template template = fm.getTemplate("login.html");
      template.process(null, response.getWriter());
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (TemplateException error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
     // Get form parameters
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String md5_password = MD5Util.getMD5String(password);

    try {
      AccountOwner owner = getOwner(username, md5_password);
      if (owner != null) {
        // Get search results and merge with template
        Map<String, Object> model = new HashMap<>();
        model.put("owner", owner);
        model.put("accounts", getAccounts(owner));
        Template template = fm.getTemplate("details.html");
        template.process(model, response.getWriter());
      }
      else {
        Template template = fm.getTemplate("invalid.html");
        template.process(null, response.getWriter());
      }
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private AccountOwner getOwner(String username, String password) throws SQLException {

    try (PreparedStatement stmt = database.prepareStatement(AUTH_QUERY)) {
      stmt.setString(1, username);
      stmt.setString(2, password);
      ResultSet results = stmt.executeQuery();
      if (results.next()) {
        AccountOwner owner = new AccountOwner();
        owner.setId(results.getInt(1));
        owner.setName(results.getString(4));
        return owner;
      }
      else {
        return null;
      }
    }
  }

  private List<Account> getAccounts(AccountOwner owner) throws SQLException {
    List<Account> accounts = new ArrayList<>();

    try (PreparedStatement stmt = database.prepareStatement(ACCOUNT_QUERY)) {
      String La = Integer.toString(owner.getId());
      stmt.setString(1, La);
      ResultSet results = stmt.executeQuery();
      while (results.next()) {
        Account acc = new Account();
        acc.setAccountNumber(results.getString(3));
        acc.setDescription(results.getString(4));
        acc.setBalance(results.getInt(5));
        accounts.add(acc);
      }
    }
    return accounts;
  }
}
