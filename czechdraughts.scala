enum Color:
    case Black
    case White

    def invert = this match
        case Black => White
        case White => Black
end Color

enum Role:
    case Pawn
    case Queen
end Role

enum Field:
    case Piece(role: Role, color: Color)
    case Empty

    override def toString = this match
        case Empty => "-"
        case Piece(Role.Queen, Color.White) => "W"
        case Piece(Role.Pawn, Color.White) => "w"
        case Piece(Role.Queen, Color.Black) => "B"
        case Piece(Role.Pawn, Color.Black) => "b"

    private def isQueen = this match
        case Piece(Role.Queen, _) => true
        case _ => false

    def isBlack = this match
        case Piece(_, Color.Black) => true
        case _ => false

    def isWhite = this match
        case Piece(_, Color.White) => true
        case _ => false

    def hasColor(other: Color) = this match
        case Piece(_, myColor) => myColor == other
        case _ => false

    def isEmpty = this == Field.Empty

    def promoted = this match
        case Piece(_, color) => Field.Piece(Role.Queen, color)
        case _ => this

    def direction = this match
        case Empty => 0
        case Piece(_, Color.White) => -1
        case Piece(_, Color.Black) => 1

    def valueFor(player: Color) = this match
        case Empty => 0
        case Piece(Role.Queen, color) => if color == player then 4 else -4
        case Piece(Role.Pawn, color) => if color == player then 1 else -1

    private def pawnStep(jump: Boolean) = if jump then 2 * direction else direction
    private def clamp(row: Int, step: Int) = if step > 7-row then 7-row else if step < -row then -row else step
    def minStep(row: Int, jump: Boolean) = clamp(row, if isQueen then -7 else pawnStep(jump))
    def maxStep(row: Int, jump: Boolean) = clamp(row, if isQueen then  7 else pawnStep(jump))
end Field

class Board(fields: Array[Field]):
    override def toString =
        0.until(8).map(row => 0.until(8).map(col => fields(row*8+col).toString).mkString("|", "|", "|")).mkString("\n")

    private def promoted =
        Board(fields.zipWithIndex.map {
            case (f, i) => if (f.isWhite && i < 8) || (f.isBlack && i >= 56) then f.promoted else f
        })

    private def distance(start: Int, end: Int) =
        math.abs(end - start) % 8

    private def path(start: Int, end: Int) =
        start.to(end).by((end - start) / distance(start, end))

    def move(start: Int, end: Int) =
        val steps = path(start, end)
        Board(fields.zipWithIndex.map {
            case (f, i) => if i == end then fields(start) else if steps.contains(i) then Field.Empty else f
        }).promoted

    private def count(start: Int, end: Int) =
        path(start, end).filter(i => !fields(i).isEmpty).length

    private def canMove(start: Int, end: Int, jump: Boolean) =
        val minDistance = if jump then 2 else 1
        fields(end).isEmpty && distance(start, end) >= minDistance && count(start, end) == minDistance

    def moves(start: Int, jump: Boolean, player: Color) =
        if !fields(start).hasColor(player) then
            Array[Int]()
        else
            val min = fields(start).minStep(start/8, jump)
            val max = fields(start).maxStep(start/8, jump)
            val ends = min.to(max).flatMap(dy => Array(start + dy*7, start + dy*9))
            ends.filter(canMove(start, _, jump)).toArray

    private def possible(player: Color, start: Int, jump: Boolean) =
        moves(start, jump, player).map(end => (move(start, end), end))

    private def walksFrom(player: Color, start: Int) =
        possible(player, start, false).map(_(0))

    private def jumpsFrom(player: Color, start: Int, first: Boolean = true): Array[Board] =
        val next = possible(player, start, true)
        if next.length > 0 then
            next.flatMap(x => x(0).jumpsFrom(player, x(1), false))
        else if !first then
            Array(this)
        else
            Array[Board]()

    def moves(player: Color) =
        lazy val jumps = 0.until(64).flatMap(jumpsFrom(player, _)).toArray
        lazy val walks = 0.until(64).flatMap(walksFrom(player, _)).toArray
        if jumps.length > 0 then jumps else walks

    def value(player: Color) =
        fields.map(f => f.valueFor(player)).sum()

end Board

object Board:
    def empty() =
        val black = Field.Piece(Role.Pawn, Color.Black)
        val white = Field.Piece(Role.Pawn, Color.White)
        val empty = Field.Empty

        Board(Array(
            empty, black, empty, black, empty, black, empty, black,
            black, empty, black, empty, black, empty, black, empty,
            empty, black, empty, black, empty, black, empty, black,
            empty, empty, empty, empty, empty, empty, empty, empty,
            empty, empty, empty, empty, empty, empty, empty, empty,
            white, empty, white, empty, white, empty, white, empty,
            empty, white, empty, white, empty, white, empty, white,
            white, empty, white, empty, white, empty, white, empty,
        ))
end Board

object Minimax:
    private val infinity = 1 << 30;

    def valueOf(board: Board, player: Color, depth: Int) =
        if depth <= 0 then
            board.value(player)
        else
            board.moves(player).map(b => -valueOf(b, player.invert, depth-1)).maxOption.getOrElse(infinity)

    def move(board: Board, player: Color, depth: Int) =
        board.moves(player).maxByOption(b => -valueOf(b, player.invert, depth-1)).getOrElse(null)
end Minimax

@main def czechdraughts() =
    var board = Board.empty
    var player = Color.White

    while
        board != null
    do
        println("Board: ")
        println(board)
        println("Score for white: ", board.valueFor(Color.White))
        println("Score for black: ", board.valueFor(Color.Black))
        println("Playing: ", player)

        board = Minimax.move(board, player, 2)
        player = player.invert

        println("\n\n")

    println("The end!")
