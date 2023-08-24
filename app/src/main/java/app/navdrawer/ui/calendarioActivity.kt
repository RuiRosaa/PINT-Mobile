package app.navdrawer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.navdrawer.R
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.annimon.stream.Stream;
import com.applandeo.materialcalendarview.CalendarUtils;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.CalendarWeekDay;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.applandeo.materialcalendarview.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



class calendarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity)

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)

        // Defina os modos de visualização: mensal, semanal, diário
        calendarView.setOnModeChangedListener { mode ->
            when (mode) {
                MaterialCalendar
                        View.MODE_MONTHS -> {
                    // Visualização Mensal
                }
                MaterialCalendarView.MODE_WEEKS -> {
                    // Visualização Semanal
                }
                MaterialCalendarView.MODE_DAY -> {
                    // Visualização Diária
                }
            }
        }

        // Lidar com a seleção de uma data (dia) no calendário
        calendarView.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                if (selected) {
                    // Exibir atividades marcadas para a data selecionada
                    val selectedDate = date.date // Data selecionada
                    // Implemente a lógica para obter e exibir as atividades para o dia selecionado
                }
            }
        })
    }
}
