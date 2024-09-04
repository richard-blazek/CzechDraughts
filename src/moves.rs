use crate::field::*;
use crate::board::*;

fn possible(board: &Board, player: Colour, start: i32, jump: bool) -> impl Iterator<Item = (i32, Board)> + '_ {
    let min_dy = board[start].min_dy(jump).clamp(-start/8, 7);
    let max_dy = board[start].max_dy(jump).clamp(-7, 7 - start / 8);

    let range = if matches!(board[start], Field::Piece(_, colour) if colour == player) {
        min_dy..(max_dy+1)
    } else {
        0..0
    };

    let ends = range.flat_map(move |dy| [start + dy * 7, start + dy * 9]);
    let valid = ends.filter(move |end| board.can_move(start, *end, jump));
    valid.map(move |end| (end, board.move_piece(start, end)))
}

fn nonjumps_from(board: &Board, player: Colour, start: i32) -> Vec<Board> {
    Vec::from_iter(possible(board, player, start, false).map(|(_, board)| board))
}

fn jumps_from(board: &Board, player: Colour, start: i32) -> Vec<Board> {
    let mut result = Vec::new();
    let mut states = vec![(start, board.clone())];

    while !states.is_empty() {
        let moves = Vec::from_iter(states.iter().flat_map(|(end, board)| possible(board, player, *end, true)));
        result.extend(moves.iter().map(|(_, board)| board));
        states = moves;
    }
    result
}

pub fn list(board: &Board, player: Colour) -> Vec<Board> {
    let jumps = Vec::from_iter((0..64).flat_map(|i| jumps_from(board, player, i)));
    if jumps.is_empty() {
        Vec::from_iter((0..64).flat_map(|i| nonjumps_from(board, player, i)))
    } else {
        jumps
    }
}
