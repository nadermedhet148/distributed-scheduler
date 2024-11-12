
resource "aws_ecr_repository" "repositories" {
  name                 = "chatbot/chatbot-sched" 
  tags = merge(var.tags, {
    "tier"                  = "cloud",
    "app"                   = "chatbot-ocs",
    "seq"                   = "1",
    "security_availability" = "private",
    "owner"                 = var.tags.owner,
    "unifonic:cost-center"  = "chatbot"
  })

  image_scanning_configuration {
    scan_on_push = true
  }
}


# Create cross-account policy
data "aws_iam_policy_document" "cross_account_policy" {
  statement {
    sid    = "full_access"
    effect = "Allow"
    principals {
      identifiers = [
        #production acocunt id
      "arn:aws:iam::118389142306:root"]
      type = "AWS"
    }
    actions = [
    "ecr:*"]
  }

}

resource "aws_ecr_repository_policy" "cross_account_policy" {
  repository = aws_ecr_repository.repositories.name
  policy     = data.aws_iam_policy_document.cross_account_policy.json
}
