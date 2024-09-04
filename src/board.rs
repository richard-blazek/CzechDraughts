use crate::field::*;
use std::ops::Index;

#[derive(Clone, Copy)]
pub struct Board([Field; 64]);

impl std::fmt::Display for Board {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let mut result = std::fmt::Result::Ok(());

        for row in 0..8 {
            for col in 0..8 {
                result = result.and(write!(f, "|{}", self.0[row * 8 + col]));
            }
            result = result.and(write!(f, "\n"));
        }
        result
    }
}

impl Index<i32> for Board {
    type Output = Field;
    fn index<'a>(&'a self, i: i32) -> &'a Field {
        &self.0[i as usize]
    }
}

impl Board {
    pub fn new() -> Board {
        let black = Field::Piece(Role::Pawn, Colour::Black);
        let white = Field::Piece(Role::Pawn, Colour::White);
        let empty = Field::Empty;

        Board([
            empty, black, empty, black, empty, black, empty, black,
            black, empty, black, empty, black, empty, black, empty,
            empty, black, empty, black, empty, black, empty, black,
            empty, empty, empty, empty, empty, empty, empty, empty,
            empty, empty, empty, empty, empty, empty, empty, empty,
            white, empty, white, empty, white, empty, white, empty,
            empty, white, empty, white, empty, white, empty, white,
            white, empty, white, empty, white, empty, white, empty,
        ])
    }

    fn promote_all(&mut self) {
        for i in 0..8 {
            if self.0[i].is_white() {
                self.0[i] = self.0[i].promoted();
            }
            if self.0[63 - i].is_black() {
                self.0[63 - i] = self.0[63 - i].promoted();
            }
        }
    }

    fn fields_between(start: i32, end: i32) -> impl Iterator<Item = i32> {
        let diff = end - start;
        let dir = diff / diff.abs() % 8;
        (start ..= end).step_by(dir as usize).map(|x| x)
    }

    pub fn move_piece(mut self, start: i32, end: i32) -> Board {
        let piece = self.0[start as usize];
        for i in Board::fields_between(start, end) {
            self.0[i as usize] = Field::Empty
        }
        self.0[end as usize] = piece;
        self.promote_all();
        self
    }

    fn count_pieces(&self, start: i32, end: i32) -> i32 {
        let steps = Board::fields_between(start, end);
        steps.map(|i| if self[i].is_empty() { 0 } else { 1 }).sum()
    }

    fn can_move(&self, start: i32, end: i32, jump: bool) -> bool {
        let min_distance = if jump {2} else {1};
        let is_free = self[end].is_empty();
        let enough_distance = (start - end).abs() % 8 >= min_distance;
        let piece_count_ok = self.count_pieces(start, end - start) == min_distance;
        is_free && enough_distance && piece_count_ok
    }

    pub fn allowed_moves(&self, start: i32, jump: bool, player: Colour) -> Vec<i32> {
        if !self[start].has_colour(player) {
            Vec::new()
        } else {
            let range = self[start].min_step(start/8, jump)..=self[start].max_step(start/8, jump);
            let ends = range.flat_map(|dy| [start + dy*7, start + dy*9]);
            Vec::from_iter(ends.filter(|end| self.can_move(start, *end, jump)))
        }
    }
}
