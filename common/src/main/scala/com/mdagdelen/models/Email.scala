package com.mdagdelen.models

import com.mdagdelen.types.Types.{Email, EmailRefined}
import com.mdagdelen.types.StringType

object Email extends StringType[EmailRefined] {
  def from(email: EmailRefined): Email = email.value.toLowerCase
}
