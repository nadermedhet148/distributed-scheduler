#Terrafrom code for ECR Repo for Spike Detection
terraform {
  backend "remote" {
    hostname     = "app.terraform.io"
    organization = "unifonic-com"

    workspaces {
      name = "unifonic-chatbot-sched"
    }
  }
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~>4.0"
    }
  }
}


provider "aws" {
 region  = "me-south-1"
}

data "aws_caller_identity" "current" {}

locals{
    account_id = data.aws_caller_identity.current.account_id
}
