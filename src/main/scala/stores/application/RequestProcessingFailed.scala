/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores.application

import io.github.pervasivecats.ValidationError

case object RequestProcessingFailed extends ValidationError {

  override val message: String = "The request processing execution has failed"
}
