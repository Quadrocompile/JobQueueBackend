package com.quadrocompile.jobqueuebackend.backend.servlets;

import com.quadrocompile.jobqueuebackend.JobQueueBackend;
import com.quadrocompile.jobqueuebackend.backend.BackendServer;
import com.quadrocompile.jobqueuebackend.model.Game;
import com.quadrocompile.jobqueuebackend.model.Job;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JobServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONObject jsonRequest = getJSONFromRequest(req);
        JSONObject answer = new JSONObject();

        switch (jsonRequest.getString("ACTION")){
            case "GETACTIVETASKS":
                answer.put("SUCCESS", "TRUE");
                answer.put("TASKCOUNT", JobQueueBackend.getScheduler().getScheduledJobs());
                break;
            case "CREATEJOB":
                String jobName = jsonRequest.getString("NAME");
                String gameCount = jsonRequest.getString("GAMECOUNT");

                if(jobName != null && gameCount != null){
                    int gameCountInt = Integer.parseInt(gameCount);

                    List<Game> gameList = new ArrayList<>();
                    for(int i = 0; i < gameCountInt; ++i){
                        gameList.add(new Game(jobName + "-Game#" + (i+1) + "/" + gameCountInt));
                    }
                    Job job = new Job(jobName, gameList);

                    JobQueueBackend.getScheduler().addJob(job);
                }

                answer.put("SUCCESS", "TRUE");
                break;
        }

        streamJSON(resp, answer);
    }

    public static String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder requestString = new StringBuilder(req.getContentLength());
        BufferedReader userRequest = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8));
        String line = null;
        while ((line = userRequest.readLine()) != null) {
            requestString.append(line);
            requestString.append("\n");
        }
        userRequest.close();
        return requestString.toString();
    }

    public static JSONObject getJSONFromRequest(HttpServletRequest req) throws IOException{
        return new JSONObject(getRequestBody(req));
    }
    public static void streamJSON(HttpServletResponse resp, JSONObject json) {
        try {
            resp.setStatus(200);
            resp.setContentType("application/json; charset=utf-8");
            OutputStreamWriter outputStream = new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8);

            json.write(outputStream);
            outputStream.flush();
        }
        catch (Exception ex){
            System.err.println("Cannot stream json: " + json.toString());
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
            catch (IOException ignored){}
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String html = "" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>WebApp Home</title>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n" +
                "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>\n" +
                "    <script type=\"text/javascript\">\n" +
                "        $(function () {\n" +
                "            // Refresh active count\n" +
                "            setInterval(function() {\n" +
                "                var request = $.post(\"/webapp/jobs\", '{\"ACTION\":\"GETACTIVETASKS\"}', function(jsonData) {\n" +
                "                    console.log(\"RESPONSE:\" + JSON.stringify(jsonData));\n" +
                "                    //if(jsonData[\"TASKCOUNT\"]){\n" +
                "                        $(\"#activeTasks\").html(\"Active Tasks: \" + jsonData[\"TASKCOUNT\"]);\n" +
                "                    //}\n" +
                "                })\n" +
                "            },1000);\n" +
                "            \n" +
                "            $(document).on('click', '#sumbit', function () {\n" +
                "                var jobName = $(\"#jobName\").val();\n" +
                "                var gameCount = $(\"#gameCount\").val();\n" +
                "                \n" +
                "                $(\"#jobName\").val(\"\");\n" +
                "                $(\"#gameCount\").val(\"\");\n" +
                "                \n" +
                "                var request = $.post(\"/webapp/jobs\", '{\"ACTION\":\"CREATEJOB\"' +\n" +
                "                ',\"NAME\":' + JSON.stringify(jobName) +\n" +
                "                ',\"GAMECOUNT\":' + JSON.stringify(gameCount) +\n" +
                "                '}', function(jsonData) {\n" +
                "                    console.log(\"RESPONSE:\" + JSON.stringify(jsonData));\n" +
                "                    if(jsonData[\"SUCCESS\"] && jsonData.SUCCESS==\"TRUE\"){\n" +
                "                        console.log(\"Jobs submitted!\");\n" +
                "                    }\n" +
                "                    else{\n" +
                "                        console.log(\"Error!\");\n" +
                "                    }\n" +
                "                })\n" +
                "                .done(function() {\n" +
                "                })\n" +
                "                .fail(function(xhr, status, error) {\n" +
                "                    console.log(error);\n" +
                "                })\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Info</h1>\n" +
                "    <span id='activeTasks'>Active Tasks: </span>\n" +
                "    <h1>Create Job</h1>\n" +
                "    Name: <input type='text' id='jobName' placeholder='Job#1'/><br>\n" +
                "    #Games: <input type='text' id='gameCount' placeholder='4'/>\n" +
                "    <div id='sumbit' style='cursor: pointer; width: 80px; height: 24px; line-height: 24px; border: solid 1px #777777; text-align: center;'>Sumbit</div>\n" +
                "</body>\n" +
                "</html>" +


                "";

        OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8);
        long payload = 0;
        try {
            writer.write(html);
            payload = html.getBytes(StandardCharsets.UTF_8).length;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        if(payload >= 0) {
            resp.setStatus(200);
            resp.setContentLength((int) payload);
            resp.setContentType("text/html; charset=utf-8");

            writer.flush();
        }
        else{
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "500 - Failed to stream requested site");
        }
    }

}
