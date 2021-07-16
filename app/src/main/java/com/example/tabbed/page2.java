package com.example.tabbed;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.ArrayUtils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link page2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class page2 extends Fragment {
    int confirm, death;
    String date, out;
    TextView t_confirm, t_death, t_date;
    GraphView graph;
    JsonObjectRequest joc;
    JSONArray ave = new JSONArray();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public page2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment page2.
     */
    // TODO: Rename and change types and number of parameters
    public static page2 newInstance(String param1, String param2) {
        page2 fragment = new page2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page2, container, false);
        t_confirm = view.findViewById((R.id.textViewConfirm));
        t_death = view.findViewById(R.id.textViewDeath);
        t_date = view.findViewById(R.id.textViewDate);
        graph = view.findViewById(R.id.graph);
        find_covid();
        findStat();
        //covid_graph(graph);
        return view;
    }

    public void find_covid() {
        String url = "https://covid19.th-stat.com/json/covid19v2/getTodayCases.json";
        joc = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String strCon = "", strDeath = "";
                    confirm = response.getInt("NewConfirmed");
                    death = response.getInt("NewDeaths");
                    date = response.getString("UpdateDate");
                    out = confirm + ", " + death + ", " + date;
                    strCon = confirm + "";
                    strDeath = death + "";
                    t_confirm.setText(strCon);
                    t_date.setText(date);
                    t_death.setText(strDeath);
                    Log.wtf("covid", out);
                } catch (JSONException e) {
                    Log.wtf("Error", "InResponseFindCov");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("ErrorRes", error);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(joc);
    }

    public void findStat() {
        String url = "https://covid19.th-stat.com/json/covid19v2/getTimeline.json";
        joc = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("Data");

                    int i = 0, total = 0, count = 0;
                    String date, month = "01";
                    while (i != array.length()) {
                        JSONObject object = array.getJSONObject(i);
                        date = object.getString("Date");
                        if (date.endsWith("20")) {
                            if (date.startsWith(month)) {
                                String newConfirm = object.getString("NewConfirmed");
                                int intcon = Integer.valueOf(newConfirm);
                                total += intcon;
                                count += 1;
                                if (month == "12") {
                                    if (count == 30) {
                                        total = total / count;
                                        ave.put(total);
                                    }
                                }
                            } else {
                                total = total / count;
                                ave.put(total);
                                month = date.substring(0, 2);
                                total = 0;
                                count = 1;
                            }
                        }
                        if (date.endsWith("21")) {
                            if (date.startsWith(month)) {
                                String newConfirm = object.getString("NewConfirmed");
                                int intcon = Integer.valueOf(newConfirm);
                                total += intcon;
                                count += 1;
                                if (i == (array.length() - 1)) {
                                    total = total / count;
                                    ave.put(total);
                                }
                            } else {
                                total = total / count;
                                ave.put(total);
                                month = date.substring(0, 2);
                                total = 0;
                                count = 1;
                            }
                        }
                        i += 1;
                    }
                    Log.wtf("Ave", ave.toString());
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                            new DataPoint(1, ave.getInt(12)),
                            new DataPoint(2, ave.getInt(13)),
                            new DataPoint(3, ave.getInt(14)),
                            new DataPoint(4, ave.getInt(15)),
                            new DataPoint(5, ave.getInt(16)),
                            new DataPoint(6, ave.getInt(17)),
                            new DataPoint(7, ave.getInt(18))
                    });
                    series.setColor(Color.RED);
                    graph.addSeries(series);
                    graph.setTitle("Average number of covid-19 infected each month in 2021");
                    graph.getGridLabelRenderer().setHorizontalAxisTitle("Month");
                    graph.getGridLabelRenderer().setVerticalAxisTitle("Infected");
                    graph.getViewport().setMaxX(8);
                    graph.getViewport().setScalable(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("ErrorRes", error);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(joc);
    }
}