variable "region" {
  description = "AWS Region"
  default     = "me-south-1"
}


variable "tags" {
  type        = map(string)
  description = "tags for the resources"
  default = {
    dc                     = "aws"
    env                    = "shared"
    project                = "internal"
    owner                  = "infrastructure"
    "unifonic:cost-center" = "chatbot"
  }
}
