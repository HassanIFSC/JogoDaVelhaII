package jogodavelha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jogodavelha.theme.JogoDaVelhaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GamerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadGamer()

        setContent {
            JogoDaVelhaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent()
                }
            }
        }
    }


    //Classe BoxModel
    class BoxModel(
        var status: Status = Status.Empty,
        var indexColumn: Int = 0,
        var indexRow: Int = 0,
    ) {
        fun showText(): String {
            return when (status) {
                Status.Empty -> ""
                Status.PlayerX -> "☓"
                Status.PlayerO -> "〇"
            }
        }
    }


    //Classe GamerModel
    class GamerModel(
        var currentPlayer: Status = Status.PlayerX,
        var winingPlayer: Status = Status.Empty,
        var isGamerEnding: Boolean = false,
    ) {}


    //Classe GamerViewModel
    class GamerViewModel : ViewModel() {
        private val gamerStatus: MutableLiveData<GamerModel> by lazy {
            MutableLiveData<GamerModel>()
        }

        private val boxes: MutableLiveData<MutableList<MutableList<BoxModel>>> by lazy {
            MutableLiveData<MutableList<MutableList<BoxModel>>>()
        }

        fun getGamerStatus(): LiveData<GamerModel> {
            return gamerStatus
        }

        fun getBoxes(): LiveData<MutableList<MutableList<BoxModel>>> {
            return boxes
        }

        fun loadGamer() {
            var indexColumn = 0
            var indexRow: Int

            boxes.value = MutableList(3) {
                indexRow = 0

                MutableList(3) {
                    BoxModel(
                        indexColumn = indexColumn++ / 3,
                        indexRow = indexRow++
                    )
                }
            }

            gamerStatus.value = GamerModel()
        }

        fun selectBox(box: BoxModel) {
            val currantPlayer: Status = gamerStatus.value?.currentPlayer!!
            var hasModification = false

            val list: MutableList<MutableList<BoxModel>> = boxes.value?.map { columns ->
                val newColumns = columns.map { row ->
                    if (box.indexColumn == row.indexColumn && box.indexRow == row.indexRow) {
                        if (row.status == Status.Empty) {
                            hasModification = true
                            row.status = currantPlayer
                        }
                    }

                    row
                }

                newColumns
            } as MutableList<MutableList<BoxModel>>

            if (hasModification && gamerStatus.value?.isGamerEnding == false) {
                gamerStatus.value?.currentPlayer = gamerStatus.value?.currentPlayer!!.next()
                boxes.value?.removeAll { true }
                boxes.value = list
            }

            checkEndingGamer()
        }

        private fun checkEndingGamer() {
            // Colunas
            (0..2).forEach() { index ->
                if (
                    boxes.value?.get(index = index)
                        ?.get(0)!!.status == boxes.value?.get(index = index)?.get(1)!!.status &&
                    boxes.value?.get(index = index)
                        ?.get(1)!!.status == boxes.value?.get(index = index)?.get(2)!!.status &&
                    boxes.value?.get(index = index)?.get(2)!!.status != Status.Empty
                ) {
                    gamerStatus.value?.isGamerEnding = true
                    gamerStatus.value?.winingPlayer =
                        boxes.value?.get(index = index)?.get(2)!!.status
                }
            }

            // Linhas
            (0..2).forEach() { index ->
                if (
                    boxes.value?.get(0)?.get(index = index)!!.status == boxes.value?.get(1)
                        ?.get(index = index)!!.status &&
                    boxes.value?.get(1)?.get(index = index)!!.status == boxes.value?.get(2)
                        ?.get(index = index)!!.status &&
                    boxes.value?.get(2)?.get(index = index)!!.status != Status.Empty
                ) {
                    gamerStatus.value?.isGamerEnding = true
                    gamerStatus.value?.winingPlayer =
                        boxes.value?.get(2)?.get(index = index)!!.status
                }
            }

            // Diagonais
            if (
                boxes.value?.get(0)?.get(0)!!.status == boxes.value?.get(1)?.get(1)!!.status &&
                boxes.value?.get(1)?.get(1)!!.status == boxes.value?.get(2)?.get(2)!!.status &&
                boxes.value?.get(2)?.get(2)!!.status != Status.Empty
            ) {
                gamerStatus.value?.isGamerEnding = true
                gamerStatus.value?.winingPlayer = boxes.value?.get(1)?.get(1)!!.status
            }

            if (
                boxes.value?.get(0)?.get(2)!!.status == boxes.value?.get(1)?.get(1)!!.status &&
                boxes.value?.get(1)?.get(1)!!.status == boxes.value?.get(2)?.get(0)!!.status &&
                boxes.value?.get(2)?.get(0)!!.status != Status.Empty
            ) {
                gamerStatus.value?.isGamerEnding = true
                gamerStatus.value?.winingPlayer = boxes.value?.get(1)?.get(1)!!.status
            }
        }
    }
    //Fim da classe GamerViewModel


    //Classe Status
    enum class Status {
        Empty,
        PlayerX,
        PlayerO;

        fun next(): Status {
            if (this.ordinal == PlayerX.ordinal) {
                return PlayerO
            }

            return PlayerX
        }
    }


    @Composable
    fun MainContent() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Jogo da Velha")
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.loadGamer()
                            }
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Reiniciar Jogo"
                            )
                        }
                    }
                )
            }
        ) {
            GridButtons()
        }
    }

    @Composable
    fun GridButtons() {
        val cards: List<List<BoxModel>> by viewModel.getBoxes().observeAsState(listOf())
        val currentGame: LiveData<GamerModel> = viewModel.getGamerStatus()
        val currentPlayer: String =
            if (currentGame.value?.currentPlayer == Status.PlayerX) "Jogador ☓" else "Jogador 〇"
        val isGamerEnding: Boolean = currentGame.value?.isGamerEnding == true
        val winingPlayer: String =
            if (currentGame.value?.winingPlayer == Status.PlayerX) "Jogador ☓" else "Jogador 〇"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White),
            verticalArrangement = Arrangement.Center,
        ) {
            cards.forEach { rows ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rows.forEach { card ->
                        ActionButton(card)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (isGamerEnding) {
                    Text(
                        text = "$winingPlayer é o vencedor! Parabéns!",
                        fontSize = 28.sp,
                        color = Color.Black,
                    )
                } else {
                    Text(
                        text = "$currentPlayer está na vez!",
                        fontSize = 28.sp,
                        color = Color.Black,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ActionButton(card: BoxModel) {
        Card(
            modifier = Modifier
                .padding(all = 10.dp)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(5.dp),
                )
                .height(100.dp)
                .width(100.dp),
            backgroundColor = Color.White,
            onClick = {
                viewModel.selectBox(card)
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = card.showText(),
                    fontSize = 34.sp,
                    color = Color.Black,
                )
            }
        }
    }
}