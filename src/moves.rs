use crate::field::*;
use crate::board::*;

fn possible(board: &Board, player: Colour, start: i32, jump: bool) -> (Vec<Board>, Vec<(i32, Board)>) {
    let ends = board.allowed_moves(start, jump, player);
    let next = Vec::from_iter(ends.iter().map(|end| (*end, board.move_piece(start, *end))));

    if next.is_empty() {
        (vec![*board], Vec::new())
    } else {
        (Vec::new(), next)
    }
}

fn nonjumps_from(board: &Board, player: Colour, start: i32) -> Vec<Board> {
    Vec::from_iter(possible(board, player, start, false).1.iter().map(|(_, board)| *board))
}

fn jumps_from(board: &Board, player: Colour, start: i32) -> Vec<Board> {
    let mut result = Vec::new();
    let mut states = vec![(start, board.clone())];

    while !states.is_empty() {
        let (halt, next): (Vec<Vec<Board>>, Vec<Vec<(i32, Board)>>) = states.iter().map(|(end, board)| possible(board, player, *end, true)).unzip();

        states = next.concat();
        result.append(&mut halt.concat());
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
