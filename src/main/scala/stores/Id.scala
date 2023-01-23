/*
 * Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
 *
 * All Rights Reserved.
 */

package io.github.pervasivecats
package stores

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative

type Id = Long Refined NonNegative
