package project.arzeit.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import project.arzeit.database.DataSource;
import project.arzeit.model.ScheduleModel;
import project.arzeit.model.User;

/**
 * project/arzeit/mypage.htmlに対応するサーブレット プロフィール出すだけ
 * 
 * @author Minoru Makino
 */
@WebServlet("/project/arzeit/main")
public class MainUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        ServletContext context = this.getServletContext();
        ScheduleModel model = new ScheduleModel((DataSource) context.getAttribute("dataSource"));

        int code = 0; // エラーコード

        request.setCharacterEncoding("UTF-8");

        // . JSON テキストを全部取り出す
        BufferedReader br = new BufferedReader(request.getReader());
        String jsonText = br.readLine();
        jsonText = URLDecoder.decode(jsonText, "UTF-8");

        System.out.println(jsonText);

        // . JSON オブジェクトに変換
        JSONObject jsonObj = new JSONObject(jsonText);
        JSONArray dateList = jsonObj.getJSONArray("date");

        // 開始、終了日時を 日付+時間に変換
        ArrayList<String> start = new ArrayList<>();
        ArrayList<String> end = new ArrayList<>();
        for (int i = 0; i < dateList.length(); i++) {
            start.add(dateList.getString(i) + " " + jsonObj.getString("start"));
            end.add(dateList.getString(i) + " " + jsonObj.getString("end"));
        }

        System.out.println(start);
        System.out.println(end);

        // 命令ごとにモデルに処理してもらう
        String saraly = jsonObj.getString("saraly");
        if (jsonObj.getString("operation").equals(model.add)) {
            code = model.setSchedule(user.getId(), start, end, saraly);

        } else {
            JSONArray s_idJSON = jsonObj.getJSONArray("s_idList");
            ArrayList<String> s_idList = new ArrayList<>(); 

            System.out.println(s_idList);

            if (jsonObj.getString("operation").equals(model.update)) {
                ArrayList<String> saralyList = new ArrayList<>();
                for (int i = 0; i < s_idJSON.length(); i++) {
                    s_idList.add(s_idJSON.getString(i));
                    saralyList.add(saraly); //いまのところ一種類の給料をみんなに割り当てるので変更の数だけ複製してる
                }

                System.out.println(saralyList);

                code = model.updateSchedule(s_idList, start, end, saralyList);

            } else if (jsonObj.getString("operation").equals(model.delete)) {
                code = model.deleteSchedule(s_idList);
            } else {
                code = 30; //命令が分からんときエラーコード
            }
        }

        System.out.println(code);

        // 送信するJSON作成
        StringBuilder json = new StringBuilder("{ \"code\": \""); // json作る
        json.append(code).append("\""); // ステータスコード
        json.append("}");
        System.out.println(json.toString());
        // JSON終わり

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.append(json.toString());
        writer.flush();
    }
}
