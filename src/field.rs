#[derive(Copy, Clone, PartialEq, Eq)]
pub enum Colour {
    Black,
    White,
}

#[derive(Copy, Clone, PartialEq, Eq)]
pub enum Role {
    Pawn,
    Queen,
}

#[derive(Copy, Clone)]
pub enum Field {
    Piece(Role, Colour),
    Empty,
}

impl Colour {
    pub fn invert(&self) -> Colour {
        match self {
            Colour::Black => Colour::White,
            Colour::White => Colour::Black,
        }
    }
}

impl std::fmt::Display for Field {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", match self {
            Field::Empty => "-",
            Field::Piece(Role::Queen, Colour::White) => "W",
            Field::Piece(Role::Pawn, Colour::White) => "w",
            Field::Piece(Role::Queen, Colour::Black) => "B",
            Field::Piece(Role::Pawn, Colour::Black) => "b",
        })
    }
}

impl Field {
    pub fn is_queen(&self) -> bool {
        matches!(self, Field::Piece(Role::Queen, _))
    }

    pub fn is_white(&self) -> bool {
        matches!(self, Field::Piece(_, Colour::White))
    }

    pub fn is_black(&self) -> bool {
        matches!(self, Field::Piece(_, Colour::Black))
    }

    pub fn has_colour(&self, colour: Colour) {
        matches!(self, Field::Piece(_, other) if colour == other)
    }

    pub fn is_empty(&self) -> bool {
        matches!(self, Field::Empty)
    }

    pub fn promoted(self) -> Field {
        match self {
            Field::Piece(_, col) => Field::Piece(Role::Queen, col),
            _ => self,
        }
    }

    pub fn direction(&self) -> i32 {
        match self {
            Field::Empty => 0,
            Field::Piece(_, Colour::White) => -1,
            Field::Piece(_, Colour::Black) => 1,
        }
    }

    pub fn min_dy(&self, jump: bool) -> i32 {
        if self.is_queen() { -7 } else if jump { 2 * self.direction() } else { self.direction() }
    }

    pub fn max_dy(&self, jump: bool) -> i32 {
        if self.is_queen() { 7 } else if jump { 2 * self.direction() } else { self.direction() }
    }
}
