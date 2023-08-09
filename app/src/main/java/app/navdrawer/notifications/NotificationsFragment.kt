package app.navdrawer.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.navdrawer.NotificationsViewModel
import app.navdrawer.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

    private var _binding3: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding3 get() = _binding3!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding3 = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding3.root

        val textView: TextView = binding3.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding3 = null
    }
}