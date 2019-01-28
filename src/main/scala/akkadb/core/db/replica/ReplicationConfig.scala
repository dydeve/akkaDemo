package akkadb.core.db.replica

/**
  * the number of nodes to replicate to
  * @param n
  */
case class N(n: Int) extends AnyVal

/**
  * the number of nodes read from before returning
  * @param r
  */
case class R(r: Int) extends AnyVal

/**
  * the number of nodes written to before considered successful
  * @param w
  */
case class W(w: Int) extends AnyVal
