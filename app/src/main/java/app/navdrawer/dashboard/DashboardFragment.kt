package app.navdrawer.dashboard

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.navdrawer.GlobalVariables
import app.navdrawer.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var username = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textGreetingHello: TextView = binding.textGreetingHello

        val sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        var period = GlobalVariables().getPeriodOfDay()
        if(period == "Manha"){
            textGreetingHello.text = "Bom-dia, $username!"
        } else if(period == "Tarde"){
            textGreetingHello.text = "Boa tarde, $username!"
        } else if(period == "Noite"){
            textGreetingHello.text = "Boa noite, $username!"
        }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            textGreetingHello.post {
                // Atualizar a vista (view) desejada aqui
                var period = GlobalVariables().getPeriodOfDay()
                if(period == "Manha"){
                    textGreetingHello.text = "Bom-dia, $username!"
                } else if(period == "Tarde"){
                    textGreetingHello.text = "Boa tarde, $username!"
                } else if(period == "Noite"){
                    textGreetingHello.text = "Boa noite, $username!"
                }
            }

            // Agendar a execução novamente após um intervalo de tempo
            handler.postDelayed(runnable, 360000) // 5 minutos
        }

        // Iniciar a execução da tarefa em segundo plano
        handler.post(runnable)
        // setupRecyclerViewOportunidades()
        //setupRecyclerViewReunioes()
        //setupRecyclerViewEquipas()
        //setupRecyclerViewCanal()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        _binding = null
    }
    /*
        private fun setupRecyclerViewOportunidades() {
            val recyclerViewOportunidades = binding.recyclerViewOportunidades
            // Configurar o RecyclerView com o adapter e o layout manager desejados
            recyclerViewOportunidades.adapter = OportunidadesAdapter()
            recyclerViewOportunidades.layoutManager = LinearLayoutManager(requireContext())
            // Adicionar dados ao adapter, se necessário
        }

        private fun setupRecyclerViewReunioes() {
            val recyclerViewReunioes = binding.recyclerViewReunioes
            // Configurar o RecyclerView com o adapter e o layout manager desejados
            recyclerViewReunioes.adapter = ReunioesAdapter()
            recyclerViewReunioes.layoutManager = LinearLayoutManager(requireContext())
            // Adicionar dados ao adapter, se necessário
        }

        private fun setupRecyclerViewEquipas() {
            val recyclerViewEquipas = binding.recyclerViewEquipas
            // Configurar o RecyclerView com o adapter e o layout manager desejados
            recyclerViewEquipas.adapter = EquipasAdapter()
            recyclerViewEquipas.layoutManager = LinearLayoutManager(requireContext())
            // Adicionar dados ao adapter, se necessário
        }

        private fun setupRecyclerViewCanal() {
            val recyclerViewCanal = binding.recyclerViewCanal
            // Configurar o RecyclerView com o adapter e o layout manager desejados
            recyclerViewCanal.adapter = CanalAdapter()
            recyclerViewCanal.layoutManager = LinearLayoutManager(requireContext())
            // Adicionar dados ao adapter, se necessário
        }
    }
    */

}

