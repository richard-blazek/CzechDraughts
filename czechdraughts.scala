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

    def isQueen = this match
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

    def value(player: Color) = this match
        case Empty => 0
        case Piece(Role.Queen, color) => if color == player then 4 else -4
        case Piece(Role.Pawn, color) => if color == player then 1 else -1
end Field

class Board(fields: Array[Field]):
    override def toString =
        0.until(8).map(row => 0.until(8).map(col => fields(row*8+col).toString).mkString("|", "|", "|")).mkString("\n")

    private def promoted =
        Board(fields.zipWithIndex.map {
            case (f, i) => if (f.isWhite && i < 8) || (f.isBlack && i >= 56) then f.promoted else f
        })

    private def distance(start: Int, end: Int) =
        math.abs(end%8 - start%8)

    private def path(start: Int, end: Int) =
        start.to(end).by((end - start) / distance(start, end))

    def move(start: Int, end: Int) =
        val steps = path(start, end)
        Board(fields.zipWithIndex.map {
            case (f, i) => if i == end then fields(start) else if steps.contains(i) then Field.Empty else f
        }).promoted

    private def count(start: Int, end: Int, color: Color) =
        path(start, end).count(fields(_).hasColor(color))

    private def isValidEnd(end: Int) =
        end >= 0 && end < 64 && fields(end).isEmpty

    private def isPathFree(start: Int, end: Int, jump: Boolean, player: Color) =
        fields(end).isEmpty
            && count(start, end, player) == 1
            && count(start, end, player.invert) == (if jump then 1 else 0)

    def possibleFrom(start: Int, jump: Boolean, player: Color) =
        if !fields(start).hasColor(player) then
            Array[Int]()
        else
            val ends =
                if fields(start).isQueen then
                    (-7).to(7)
                        .filter(dy => math.abs(dy) < (if jump then 2 else 1))
                        .flatMap(dy => Array(start + dy*7, start + dy*9))
                        .toArray
                else 
                    Array(1, -1)
                        .map(d => start + (8 * fields(start).direction + d) * (if jump then 2 else 1))

            ends.filter(end => isValidEnd(end) && isPathFree(start, end, jump, player))
                .filter(end => math.abs(end/8 - start/8) == math.abs(end%8 - start%8))

    private def movesFrom(player: Color, start: Int, jump: Boolean) =
        possibleFrom(start, jump, player).map(end => (move(start, end), end))

    private def walksFrom(player: Color, start: Int) =
        movesFrom(player, start, false).map(_(0))

    private def jumpsFrom(player: Color, start: Int, first: Boolean = true): Array[Board] =
        val next = movesFrom(player, start, true)
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
        fields.map(f => f.value(player)).sum()

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

    def valueOf(board: Board, player: Color, depth: Int): Int =
        if depth <= 0 then
            board.value(player)
        else
            board.moves(player).map(b => -valueOf(b, player.invert, depth-1)).maxOption.getOrElse(infinity)

    def move(board: Board, player: Color, depth: Int) =
        board.moves(player).maxByOption(b => -valueOf(b, player.invert, depth-1)).getOrElse(null)
end Minimax

@main def czechdraughts() =
    var board = Board.empty()
    var player = Color.White

    while
        board != null
    do
        println("Board: ")
        println(board)
        println("Score for white: " + board.value(Color.White))
        println("Score for black: " + board.value(Color.Black))
        println("Playing: " + player)

        board = Minimax.move(board, player, 2)
        player = player.invert

        scala.io.StdIn.readLine()

    println("The end!")
