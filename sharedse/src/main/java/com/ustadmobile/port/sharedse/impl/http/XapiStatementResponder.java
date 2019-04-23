package com.ustadmobile.port.sharedse.impl.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.contentformats.xapi.Statement;
import com.ustadmobile.core.contentformats.xapi.StatementDeserializer;
import com.ustadmobile.core.contentformats.xapi.StatementSerializer;
import com.ustadmobile.core.contentformats.xapi.endpoints.StatementEndpoint;
import com.ustadmobile.core.contentformats.xapi.endpoints.StatementRequestException;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class XapiStatementResponder implements RouterNanoHTTPD.UriResponder {

    private static final Type STATEMENT_LIST_TYPE = new TypeToken<ArrayList<Statement>>() {
    }.getType();

    public static final String PARAM_STATEMENT_ID = "statementId";
    private static final String PARAM_VOID_STATEMENT_ID = "voidedStatementId";
    private static final String PARAM_ATTACHMENTS = "attachments";
    private static final String PARAM_FORMAT = "format";

    private static final String[] WANTED_KEYS = new String[]{PARAM_STATEMENT_ID, PARAM_VOID_STATEMENT_ID, PARAM_ATTACHMENTS, PARAM_FORMAT};

    private static final int PARAM_APPREPO_INDEX = 0;


    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UmAppDatabase repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase.class);


        if (urlParams.containsKey(PARAM_STATEMENT_ID) || urlParams.containsKey(PARAM_VOID_STATEMENT_ID)) {

            // single statement
            if (urlParams.containsKey(PARAM_STATEMENT_ID) && urlParams.containsKey(PARAM_VOID_STATEMENT_ID)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                        "application/octet", null);
            }

            Set<String> keyList = urlParams.keySet();
            List<String> wantedList = Arrays.asList(WANTED_KEYS);
            for (String key : keyList) {
                if (!wantedList.contains(key)) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                            "application/octet", null);
                }
            }

        } else {

            // list of statements


        }


        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                "application/octet", null);
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UmAppDatabase repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase.class);

        Gson gson = createGson();
        byte[] content = null;
        FileInputStream fin = null;
        ByteArrayOutputStream bout = null;
        String tmpFileName;
        try {

            String statement = null;
            Map<String, String> map = new HashMap<>();
            session.parseBody(map);
            if (map.containsKey("content")) {
                tmpFileName = map.get("content");
                fin = new FileInputStream(tmpFileName);
                bout = new ByteArrayOutputStream();
                UMIOUtils.readFully(fin, bout);
                bout.flush();
                content = bout.toByteArray();
            } else {
                statement = session.getQueryParameterString();
            }
            Map<String, List<String>> queryParams = session.getParameters();
            String statementId = "";
            if (queryParams != null && queryParams.containsKey("statementId")) {
                statementId = queryParams.get("statementId").get(0);
            }

            if (content != null || statement != null) {
                statement = content != null ? new String(content) : statement;

                ArrayList<Statement> statements = getStatementsFromJson(statement.trim(), gson);
                StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
                endpoint.storeStatements(statements, statementId);
            } else {
                throw new StatementRequestException("no content found", 204);
            }
        } catch (StatementRequestException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(e.getErrorCode()),
                    "application/octet", e.getMessage());
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.getMessage());
        } finally {
            UMIOUtils.INSTANCE.closeQuietly(fin);
            UMIOUtils.INSTANCE.closeQuietly(bout);

        }
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT,
                "application/octet", null);
    }

    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Statement.class, new StatementSerializer());
        builder.registerTypeAdapter(Statement.class, new StatementDeserializer());
        return builder.create();
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UmAppDatabase repo = uriResource.initParameter(PARAM_APPREPO_INDEX, UmAppDatabase.class);

        Gson gson = createGson();
        List<String> uuids;
        InputStream is = null;
        try {

            Map<String, List<String>> queryParams = session.getParameters();
            if (queryParams != null && queryParams.containsKey("method")) {

                String method = queryParams.get("method").get(0);
                if (method.equalsIgnoreCase("put")) {
                    return put(uriResource, urlParams, session);
                } else if (method.equalsIgnoreCase("get")) {
                    return get(uriResource, urlParams, session);
                }

            }
            Map<String, String> map = new HashMap<>();
            session.parseBody(map);
            String statement = session.getQueryParameterString().trim();

            ArrayList<Statement> statements = getStatementsFromJson(statement, gson);

            StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
            uuids = endpoint.storeStatements(statements, "");
            is = new ByteArrayInputStream(gson.toJson(uuids).getBytes());

            return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                    "application/octet", is);

        } catch (IOException | NanoHTTPD.ResponseException e) {
            if (e.getMessage() != null && e.getMessage().equals("Has Existing Statements")) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.CONFLICT,
                        "application/octet", null);
            }
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", e.getMessage());
        } finally {
            UMIOUtils.INSTANCE.closeQuietly(is);
        }

    }

    private ArrayList<Statement> getStatementsFromJson(String statement, Gson gson) {
        ArrayList<Statement> statements = new ArrayList<>();
        if (statement.startsWith("{")) {
            Statement obj = gson.fromJson(statement, Statement.class);
            statements.add(obj);
        } else {
            statements.addAll(gson.fromJson(statement, STATEMENT_LIST_TYPE));
        }
        return statements;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    public class StatementResult {

        private List<Statement> statements;

        private String more;

        public StatementResult(List<Statement> list, String more) {
            this.more = more;
            this.statements = list;
        }

        public List<Statement> getStatements() {
            return statements;
        }

        public void setStatements(List<Statement> statements) {
            this.statements = statements;
        }

        public String getMore() {
            return more;
        }

        public void setMore(String more) {
            this.more = more;
        }
    }
}
