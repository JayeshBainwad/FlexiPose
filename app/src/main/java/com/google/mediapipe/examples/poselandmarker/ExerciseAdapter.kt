import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.poselandmarker.activities.ExerciseType
import com.google.mediapipe.examples.poselandmarker.activities.MainActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ItemExerciseCardBinding

class ExerciseAdapter(
    private val context: MainActivity,
    private val exerciseList: List<ExerciseType>,
    private val onExerciseClick: (ExerciseType) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(private val binding: ItemExerciseCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(exercise: ExerciseType) {
            binding.tvExerciseName.text = exercise.name
            binding.root.setOnClickListener {
                onExerciseClick(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exerciseList[position])
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }
}
