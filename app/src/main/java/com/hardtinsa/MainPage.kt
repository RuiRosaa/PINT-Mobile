package com.hardtinsa

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.sft4all.BeneficiosActivity
import com.sft4all.CalendarioActivity
import com.sft4all.ClientesActivity
import com.instamobile.kotlinlogin.R
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.SubcolumnValue
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.PieChartView
import org.json.JSONException
import java.time.format.TextStyle
import java.util.*

class MainPage : BaseActivity() {
    private lateinit var indicatorData: IndicatorData
    private lateinit var oportunidadesData: OportunidadesData
    private lateinit var vagasData: VagasData
    private lateinit var labeluserID: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val bemvindoTextView = findViewById<TextView>(R.id.bemvindo)
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val greetingMessage = when {
            currentHour in 5..12 -> "Bom Dia"
            currentHour in 13..19 -> "Boa Tarde"
            else -> "Boa Noite"
        }
        bemvindoTextView.text = greetingMessage

        labeluserID = findViewById(R.id.user)
        val nome = Globals.username
        labeluserID.text = nome
        Log.d("MainPage", "Nome: $nome")

        fetchIndicatorData()

        val internetStatusImageView = findViewById<ImageView>(R.id.internet_status)
        updateInternetStatusIcon(internetStatusImageView)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            handleBottomNavigationItemSelected(menuItem)
            true

        }

        val isAutenticado = Globals.autenticado
        if (!isAutenticado) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    private fun fetchIndicatorData() {
        val apiurl = Globals.apiurl
        val iduser = Globals.userID
        val token = Globals.token
        val url1 = apiurl + "indicador/resumouser/$iduser"
        val url2 = apiurl + "indicador/oportunidadenegocio1/$iduser"
        val url3 = apiurl + "indicador/oferta1/$iduser/$token"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest1 = JsonObjectRequest(
            Request.Method.GET, url1, null,
            { response ->
                // Parse the response and extract the indicator data
                val data = response.optJSONArray("data")?.optJSONObject(0)
                if (data != null) {
                    indicatorData = IndicatorData(
                        data.optInt("NOfertasAtivas"),
                        data.optInt("NBeneficiosAtivos"),
                        data.optInt("NOportunidadesAtivas"),
                        data.optInt("NCandidaturasAtivas"),
                        data.optInt("NIdeiaAtivas"),
                        data.optInt("NClientesAtivos"),
                        data.optInt("NContatosAtivos"),
                        data.optInt("NEntrevistasAtivas"),
                        data.optInt("NAvisosAtivos")
                    )

                    // Update the UI with the indicator data
                    updateIndicatorCounters()
                }
            },
            { error ->
                // Handle the error
                // ...
            }
        )
        val jsonObjectRequest2 = JsonObjectRequest(
            Request.Method.GET, url2, null,
            { response ->
                val data = response.optJSONArray("data")

                if (data != null && data.length() > 0) {
                    val tiponegocioList = ArrayList<String>()
                    val nProjectosList = ArrayList<Int>()

                    for (i in 0 until data.length()) {
                        val item = data.optJSONObject(i)
                        val tiponegocio = item.optString("TIPONEGOCIO")
                        val nProjectos = item.optInt("NProjectos")

                        tiponegocioList.add(tiponegocio)
                        nProjectosList.add(nProjectos)
                    }

                        // Store the retrieved data for the pie chart
                        oportunidadesData = OportunidadesData(tiponegocioList, nProjectosList)

                        // Update the pie chart
                        updatePieChart()
                    }
            },
            { error ->
                // Handle the error
                // ...
            }
        )
        val jsonObjectRequest3 = JsonObjectRequest(
            Request.Method.GET, url3, null,
            { response ->
                val data = response.optJSONArray("data")

                if (data != null && data.length() > 0) {
                    val descricaoList = ArrayList<String>()
                    val ncandidatosList = ArrayList<Int>()

                    for (i in 0 until data.length()) {
                        val item = data.optJSONObject(i)
                        val descricao = item.optString("DESCRICAO")
                        val ncandidatos = item.optInt("NCANDIDATOS")

                        descricaoList.add(descricao)
                        ncandidatosList.add(ncandidatos)
                    }

                    // Store the retrieved data for the bar chart
                    vagasData = VagasData(descricaoList, ncandidatosList)

                    // Update the bar chart
                    updateBarChart()
                }
            },
            { error ->
                // Handle the error
                // ...
            }
        )

        requestQueue.add(jsonObjectRequest1)
        requestQueue.add(jsonObjectRequest2)
        requestQueue.add(jsonObjectRequest3)
    }

    private fun updateIndicatorCounters() {
        val nOfertasAtivas = indicatorData.nOfertasAtivas
        val nBeneficiosAtivos = indicatorData.nBeneficiosAtivos
        val nOportunidadesAtivas = indicatorData.nOportunidadesAtivas
        val nCandidaturasAtivas = indicatorData.nCandidaturasAtivas
        val nIdeiaAtivas = indicatorData.nIdeiaAtivas
        val nClientesAtivos = indicatorData.nClientesAtivos
        val nContatosAtivos = indicatorData.nContatosAtivos
        val nEntrevistasAtivas = indicatorData.nEntrevistasAtivas
        val nAvisosAtivos = indicatorData.nAvisosAtivos


        val nOportunidadesAtivasTextView = findViewById<TextView>(R.id.nOportunidadesAtivas)
        nOportunidadesAtivasTextView.text = nOportunidadesAtivas.toString()

        val nCandidaturasAtivasTextView = findViewById<TextView>(R.id.nCandidaturasAtivas)
        nCandidaturasAtivasTextView.text = nCandidaturasAtivas.toString()

        val nVagasAtivasTextView = findViewById<TextView>(R.id.nVagas)
        nVagasAtivasTextView.text = nOfertasAtivas.toString()


        val totalCount = nOfertasAtivas + nBeneficiosAtivos + nOportunidadesAtivas +
                nCandidaturasAtivas + nIdeiaAtivas + nClientesAtivos + nContatosAtivos + nEntrevistasAtivas + nAvisosAtivos
    }

    private fun updatePieChart() {
        val tiponegocioList = oportunidadesData.tiponegocioList
        val nProjectosList = oportunidadesData.nProjectosList

        val totalProjects = nProjectosList.sum().toFloat()

        val pieData = ArrayList<SliceValue>()

        for (i in tiponegocioList.indices) {
            val tiponegocio = tiponegocioList[i]
            val nProjectos = nProjectosList[i]

            val percentage = (nProjectos.toFloat() / totalProjects) * 100

            val sliceValue = SliceValue(nProjectos.toFloat())
            sliceValue.setLabel("$tiponegocio: ${"%.2f".format(percentage)}%")
            sliceValue.color = generateRandomColor()
            pieData.add(sliceValue)
        }

        val pieChartData = PieChartData(pieData)
        pieChartData.setHasLabels(true)
        pieChartData.setHasLabelsOutside(false)
        pieChartData.setHasCenterCircle(false)


        val pieChartView = findViewById<PieChartView>(R.id.chart)
        pieChartView.pieChartData = pieChartData

        // Refresh the chart
        pieChartView.startDataAnimation()

        // Mostra ou esconde o piechart
        if (Globals.perfilID < 4) {
            pieChartView.visibility = View.GONE
        } else {
            pieChartView.visibility = View.VISIBLE
        }
    }

    private fun updateBarChart() {
        val descricaoList = vagasData.descricaoList
        val ncandidatosList = vagasData.ncandidatosList

       //verifica se o idperfil é superior ou igual a 4
        if (Globals.perfilID >= 4) {

            val columnData = ArrayList<Column>()
            val axisValues = ArrayList<AxisValue>()

            for (i in descricaoList.indices) {
                val descricao = descricaoList[i]
                val ncandidatos = ncandidatosList[i]

                val columns = ArrayList<SubcolumnValue>()
                val subcolumnValue = SubcolumnValue(ncandidatos.toFloat())
                subcolumnValue.color = generateRandomColor()
                columns.add(subcolumnValue)

                val column = Column(columns)
                column.setHasLabels(true)
                column.values = columns
                column.setHasLabelsOnlyForSelected(true)

                val axisValue = AxisValue(i.toFloat())
                axisValue.label = descricao.toCharArray() // Convert to CharArray
                axisValues.add(axisValue)
                columnData.add(column)
            }

            val columnChartData = ColumnChartData(columnData)
            val axisX = Axis()
            val axisY = Axis().setHasLines(true)
            columnChartData.axisXBottom = axisX
            columnChartData.axisYLeft = axisY

            axisX.name = "Vagas"
            axisX.setTextColor(Color.BLACK)
            axisY.name = "Ncandidatos"
            axisY.setTextColor(Color.BLACK)

            // Create the bar chart view and set the data
            val barChartView = findViewById<ColumnChartView>(R.id.chart2)
            barChartView.columnChartData = columnChartData

            // Set the value select listener
            barChartView.onValueTouchListener = object : ColumnChartOnValueSelectListener {
                override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
                    // Recolhe o  selected descricao
                    val selectedDescricao = descricaoList[columnIndex]

                    // Mostra o selected descricao
                    val selectedDescricaoTextView = findViewById<TextView>(R.id.selectedDescricaoTextView)
                    selectedDescricaoTextView.text = selectedDescricao
                }

                override fun onValueDeselected() {

                }
            }

            barChartView.startDataAnimation()
        } else {


            val barChartView = findViewById<ColumnChartView>(R.id.chart2)
            barChartView.visibility = View.GONE
        }
    }

    private fun generateRandomColor(): Int {
        val red = (0..255).random()
        val green = (0..255).random()
        val blue = (0..255).random()
        return Color.rgb(red, green, blue)
    }


data class IndicatorData(
    val nOfertasAtivas: Int,
    val nBeneficiosAtivos: Int,
    val nOportunidadesAtivas: Int,
    val nCandidaturasAtivas: Int,
    val nIdeiaAtivas: Int,
    val nClientesAtivos: Int,
    val nContatosAtivos: Int,
    val nEntrevistasAtivas: Int,
    val nAvisosAtivos: Int
)
    data class OportunidadesData(
        val tiponegocioList: List<String>,
        val nProjectosList: List<Int>
    )
    data class VagasData(
        val descricaoList: List<String>,
        val ncandidatosList: List<Int>
    )

}
