/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.store.valueobjects

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative

type CountInt = Int Refined NonNegative

trait Count {

  val value: CountInt
}
